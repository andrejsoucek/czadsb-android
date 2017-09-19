package cz.adsb.czadsb.utils

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.exceptions.ClientErrorException
import cz.adsb.czadsb.exceptions.InvalidCredentialsException
import cz.adsb.czadsb.exceptions.RedirectionException
import cz.adsb.czadsb.exceptions.ServerErrorException
import cz.adsb.czadsb.model.User

object UserManager {

    private val PREFS_ACCOUNT = "account_preferences"
    private val PREFS_ACCOUNT_NAME = "name"
    private val PREFS_ACCOUNT_PASSWORD = "pass"
    private val USER_NOT_LOGGED_IN = "x_no_user"

    fun login(ctx: Context, user: User): Boolean {
        val (_, response, _) = Fuel.get(ctx.getProperty("login_check"))
                .authenticate(user.name, user.pass)
                .timeout(5000)
                .response()
        val sc = response.httpStatusCode
        return when (sc) {
            in 200..299 -> {saveUser(ctx, user); true}
            in 300..399 -> throw RedirectionException("Redirection error, status code: $sc")
            401, 403 -> throw InvalidCredentialsException("User is not authorized, status code: $sc")
            in 400..499 -> throw ClientErrorException("Client error, status code: $sc")
            in 500..599 -> throw ServerErrorException("Server error, status code: $sc")
            else -> false
        }
    }

    fun logout(ctx: Context) {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCOUNT_NAME, USER_NOT_LOGGED_IN)
        editor.putString(PREFS_ACCOUNT_PASSWORD, "")
        editor.apply()
    }

    fun isUserLoggedIn(ctx: Context): Boolean = getUser(ctx).name != USER_NOT_LOGGED_IN

    fun getUser(ctx: Context): User {
        val prefs = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
        return User(prefs.getString(PREFS_ACCOUNT_NAME, USER_NOT_LOGGED_IN), prefs.getString(PREFS_ACCOUNT_PASSWORD, ""))
    }

    private fun saveUser(ctx: Context, user: User) {
        val editor = ctx.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE).edit()
        editor.putString(PREFS_ACCOUNT_NAME, user.name)
        editor.putString(PREFS_ACCOUNT_PASSWORD, user.pass)
        editor.apply()
    }
}