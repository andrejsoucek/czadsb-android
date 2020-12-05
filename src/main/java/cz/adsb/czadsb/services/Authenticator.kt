package cz.adsb.czadsb.services

import android.app.Activity
import com.auth0.android.Auth0
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import cz.adsb.czadsb.R

class Authenticator(private val auth0: Auth0) {
    fun authenticate(activity: Activity, callback: AuthCallback) {
        WebAuthProvider.login(auth0)
            .withScheme("demo")
            .withScope("openid offline_access")
            .withAudience(
                String.format(
                    "https://%s/userinfo",
                    activity.applicationContext.getString(R.string.com_auth0_domain)
                )
            )
            .start(activity, callback)
    }
}