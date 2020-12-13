package cz.adsb.czadsb.model.user

import android.app.Activity
import android.app.Dialog
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials

class Authenticator constructor(
    private val ctx: Context,
    private val auth0: Auth0 = Auth0(ctx) // @TODO DI
) {
    init {
        this.auth0.isOIDCConformant = true
    }

    fun login(returnActivity: Activity, callback: AuthCallback)
    {
        WebAuthProvider
            .login(this.auth0)
            .withScheme("demo")
            .withScope("openid offline_access")
//            .withAudience("https://czadsb.cz/private/AircraftList.json")
            .start(returnActivity, callback)
    }

    fun logout(returnActivity: Activity, callback: VoidCallback)
    {
        WebAuthProvider
            .logout(this.auth0)
            .withScheme("demo")
            .start(returnActivity, callback)
    }

    fun saveUserCredentials(credentials: Credentials)
    {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCESS_TOKEN, credentials.accessToken)
        editor.apply()
    }

    fun deleteUserCredentials()
    {
        val editor = this@Authenticator.ctx.getSharedPreferences(Authenticator.PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.remove(PREFS_ACCESS_TOKEN)
        editor.apply()
    }

    fun getAccessToken(): String?
    {
        val prefs = this.ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_ACCESS_TOKEN, null)
    }

    fun isUserLoggedIn(): Boolean = getAccessToken() != null

    private companion object UserPreferences {
        private const val PREFS_ACCOUNT = "account_preferences"
        private const val PREFS_ACCESS_TOKEN = "access_token"
    }
}