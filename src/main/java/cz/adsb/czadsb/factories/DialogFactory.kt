package cz.adsb.czadsb.factories

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import cz.adsb.czadsb.R
import cz.adsb.czadsb.exceptions.ClientErrorException
import cz.adsb.czadsb.exceptions.InvalidCredentialsException
import cz.adsb.czadsb.exceptions.RedirectionException
import cz.adsb.czadsb.exceptions.ServerErrorException
import cz.adsb.czadsb.model.User
import cz.adsb.czadsb.utils.UserManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread

object DialogFactory {

    fun createLoginDialog(parentView: View): AlertDialog {
        val alert = AlertDialog.Builder(parentView.context)
        val inflater = parentView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.login_dialog, null)
        alert.setTitle(R.string.Login)
        alert.setView(layout)
        alert.setCancelable(true)
        alert.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        alert.setPositiveButton(R.string.Login) { dialog, _ ->
            val user = User(layout.find<EditText>(R.id.et_username).text.toString(), layout.find<EditText>(R.id.et_password).text.toString())
            var loginTextResource: Int
            doAsync {
                try {
                    val login = UserManager.login(parentView.context.applicationContext, user)
                    loginTextResource = if (login) {
                        R.string.login_successful
                    } else {
                        R.string.unexpected_error_while_logging_in
                    }
                } catch (e: ClientErrorException) {
                    loginTextResource = R.string.could_not_login_client_error
                } catch (e: InvalidCredentialsException) {
                    loginTextResource = R.string.name_or_password_wrong
                } catch (e: RedirectionException) {
                    loginTextResource = R.string.could_not_login_server_redirection_error
                } catch (e: ServerErrorException) {
                    loginTextResource = R.string.could_not_login_server_error
                } catch (e: Exception) {
                    loginTextResource = R.string.unexpected_error_while_logging_in
                }
                uiThread {
                    dialog.cancel()
                    parentView.context.toast(loginTextResource)
                } //TODO
            }
        }
        return alert.create()
    }
}