package cz.adsb.czadsb.model

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.exceptions.ClientErrorException
import cz.adsb.czadsb.exceptions.InvalidCredentialsException
import cz.adsb.czadsb.exceptions.RedirectionException
import cz.adsb.czadsb.exceptions.ServerErrorException
import cz.adsb.czadsb.utils.getProperty

object User {
    private const val PREFS_ACCOUNT = "account_preferences"
    private const val PREFS_ACCOUNT_NAME = "name"
    private const val PREFS_ACCOUNT_PASSWORD = "pass"

    fun login(ctx: Context, name: String, pass: String): Boolean
    {
        val (_, response, _) = Fuel.get(ctx.getProperty("login_check"))
            .timeout(5000)
            .response()
        val sc = response.statusCode
        return when (sc) {
            in 200..299 -> {saveIdentity(ctx, name, pass); true}
            in 300..399 -> throw RedirectionException("Redirection error, status code: $sc")
            401, 403 -> throw InvalidCredentialsException("User is not authorized, status code: $sc")
            in 400..499 -> throw ClientErrorException("Client error, status code: $sc")
            in 500..599 -> throw ServerErrorException("Server error, status code: $sc")
            else -> false
        }
    }

    fun logout(ctx: Context)
    {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCOUNT_NAME, null)
        editor.putString(PREFS_ACCOUNT_PASSWORD, null)
        editor.apply()
    }

    fun isLoggedIn(ctx: Context): Boolean = getIdentity(ctx) != null

    fun getIdentity(ctx: Context): String?
    {
        val prefs = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
        return prefs.getString(PREFS_ACCOUNT_NAME, null)
    }

    private fun saveIdentity(ctx: Context, name: String, pass: String)
    {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCOUNT_NAME, name)
        editor.putString(PREFS_ACCOUNT_PASSWORD, pass)
        editor.apply()
    }
}