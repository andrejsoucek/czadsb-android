package cz.adsb.czadsb.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cz.adsb.czadsb.R
import cz.adsb.czadsb.utils.observeEvent
import cz.adsb.czadsb.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val userViewModel by viewModel<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.userViewModel.userLoggedIn.observeEvent(this@LoginActivity, {
            Toast.makeText(
                applicationContext,
                if (it) R.string.login_successful else R.string.you_have_been_logged_out,
                Toast.LENGTH_SHORT
            ).show()
            if (it) {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                this.userViewModel.performLogin(this@LoginActivity)
            }
        })
    }
}