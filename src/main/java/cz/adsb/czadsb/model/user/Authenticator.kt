package cz.adsb.czadsb.model.user

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import cz.adsb.czadsb.utils.getProperty

class Authenticator constructor(
    private val ctx: Context,
    private val auth0: Auth0
) {
    fun login(returnActivity: Activity, callback: AuthCallback) {
        WebAuthProvider
            .login(this.auth0)
            .withScheme("demo")
            .withScope("openid profile email offline_access")
            .withAudience(this.ctx.getProperty("aircraftlist_url"))
            .start(returnActivity, callback)
    }

    fun refresh() {
        val refreshToken = this.getRefreshToken()
        if (refreshToken != null) {
            val client = AuthenticationAPIClient(this.auth0)
            client
                .renewAuth(refreshToken)
                .start(object : BaseCallback<Credentials, AuthenticationException> {
                    override fun onFailure(error: AuthenticationException) {
                        throw error
                    }

                    override fun onSuccess(payload: Credentials?) {
                        if (payload != null) {
                            this@Authenticator.saveUserCredentials(payload)
                        }
                    }

                })
        }
    }

    fun logout(returnActivity: Activity, callback: VoidCallback) {
        WebAuthProvider
            .logout(this.auth0)
            .withScheme("demo")
            .start(returnActivity, callback)
    }

    fun saveUserCredentials(credentials: Credentials) {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCESS_TOKEN, credentials.accessToken)
        editor.putString(PREFS_REFRESH_TOKEN, credentials.refreshToken)
        editor.apply()
    }

    fun deleteUserCredentials() {
        val editor = this@Authenticator.ctx.getSharedPreferences(
            PREFS_ACCOUNT,
            Context.MODE_PRIVATE
        ).edit()
        editor.remove(PREFS_ACCESS_TOKEN)
        editor.apply()
    }

    fun getAccessToken(): String? {
        val prefs = this.ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_ACCESS_TOKEN, null)
    }

    fun isUserLoggedIn(): Boolean = getAccessToken() != null

    private fun getRefreshToken(): String? {
        val prefs = this.ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_REFRESH_TOKEN, null)
    }

    private companion object UserPreferences {
        private const val PREFS_ACCOUNT = "account_preferences"
        private const val PREFS_ACCESS_TOKEN = "access_token"
        private const val PREFS_REFRESH_TOKEN = "refresh_token"
    }
}