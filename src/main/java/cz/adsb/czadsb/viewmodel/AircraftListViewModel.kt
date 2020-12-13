package cz.adsb.czadsb.viewmodel

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.auth0.android.Auth0
import cz.adsb.czadsb.model.api.PlanesAPI
import cz.adsb.czadsb.model.planes.AircraftList
import cz.adsb.czadsb.model.user.Authenticator
import cz.adsb.czadsb.utils.Event
import cz.adsb.czadsb.utils.getProperty
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

class AircraftListViewModel(application: Application) : AndroidViewModel(application) {

    val event = MutableLiveData<Event<Unit>>()

    var lastDv = ""

    private val timer = timer(
        "fetch_planes",
        false,
        0,
        getRefreshRate(application.applicationContext)
    ) {
        Handler(Looper.getMainLooper()).post {
            this@AircraftListViewModel.event.value = Event(Unit)
        }
    }

    private val api: PlanesAPI = PlanesAPI(
        Authenticator(application.applicationContext, Auth0(application.applicationContext)),
        application.applicationContext.getProperty("aircraftlist_url")
    ) //@TODO DI

    suspend fun refreshAircraftList(north: Double, south: Double, west: Double, east: Double): AircraftList
    {
       val aircraftList = this@AircraftListViewModel.api.fetch(
           this.lastDv,
           north,
           south,
           west,
           east
       )
        this.lastDv = aircraftList.lastDv

        return aircraftList
    }

    private fun getRefreshRate(ctx: Context): Long
    {
        return ctx.getProperty("refresh_rate").toLong()
    }

    override fun onCleared() {
        super.onCleared()
        this.timer.cancel()
        this.timer.purge()
    }
}