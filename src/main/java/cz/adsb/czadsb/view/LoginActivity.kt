package cz.adsb.czadsb.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.adsb.czadsb.utils.observeEvent
import cz.adsb.czadsb.viewmodel.UserViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private val userViewModel by viewModel<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.userViewModel.hasAccessToken.observeEvent(this@LoginActivity, {
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