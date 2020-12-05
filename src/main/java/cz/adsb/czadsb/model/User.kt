package cz.adsb.czadsb.model

import android.content.Context
import com.auth0.android.result.Credentials


object User {
    private const val PREFS_ACCOUNT = "account_preferences"
    private const val PREFS_ACCESS_TOKEN = "access_token"

    fun login(ctx: Context, credentials: Credentials) {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCESS_TOKEN, credentials.accessToken)
        editor.apply()
    }

    fun logout(ctx: Context)
    {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.remove(PREFS_ACCESS_TOKEN)
        editor.apply()
    }

    fun isLoggedIn(ctx: Context): Boolean = getIdentity(ctx) != null

    fun getIdentity(ctx: Context): String?
    {
        val prefs = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_ACCESS_TOKEN, null)
    }
}