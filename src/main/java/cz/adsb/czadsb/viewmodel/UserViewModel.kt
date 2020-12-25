package cz.adsb.czadsb.viewmodel

import android.app.Activity
import android.app.Application
import android.app.Dialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.VoidCallback
import com.auth0.android.result.Credentials
import cz.adsb.czadsb.model.user.Authenticator
import cz.adsb.czadsb.utils.Event

class UserViewModel(
    application: Application,
    private val authenticator: Authenticator,
) : AndroidViewModel(application) {

    val userLoggedIn: LiveData<Event<Boolean>>
        get() = this._userLoggedIn

    private val _userLoggedIn = MutableLiveData<Event<Boolean>>()

    init {
        this._userLoggedIn.value = Event(this.authenticator.isUserLoggedIn())
    }

    fun performLogin(returnActivity: Activity) {
        this.authenticator.login(returnActivity, object : AuthCallback {
            override fun onFailure(dialog: Dialog) {
                this@UserViewModel._userLoggedIn.postValue(Event(false))
            }

            override fun onFailure(exception: AuthenticationException) {
                this@UserViewModel._userLoggedIn.postValue(Event(false))
                // TODO do something
            }

            override fun onSuccess(credentials: Credentials) {
                this@UserViewModel.authenticator.saveUserCredentials(credentials)
                this@UserViewModel._userLoggedIn.postValue(Event(true))
            }
        })
    }

    fun performLogout(returnActivity: Activity) {
        this.authenticator.logout(returnActivity, object : VoidCallback {
            override fun onSuccess(payload: Void?) {
                this@UserViewModel.authenticator.deleteUserCredentials()
                this@UserViewModel._userLoggedIn.postValue(Event(false))
            }

            override fun onFailure(error: Auth0Exception) {
                this@UserViewModel._userLoggedIn.postValue(Event(true))
                // TODO Show error to user
            }
        })
    }
}