package cz.adsb.czadsb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import cz.adsb.czadsb.R
import cz.adsb.czadsb.model.api.ImagesAPI
import cz.adsb.czadsb.model.images.Image
import cz.adsb.czadsb.model.planes.Aircraft
import cz.adsb.czadsb.utils.Event
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class AircraftInfoViewModel(application: Application) : AndroidViewModel(application) {

    val state = MutableLiveData<State>()

    val selectedAircraft = MutableLiveData<Aircraft>()

    val track = MutableLiveData<List<GeoPoint>>()

    val image = MutableLiveData<Image?>()

    val error: LiveData<Event<String>>
        get() = this._error

    private val _error = MutableLiveData<Event<String>>()

    private val api = ImagesAPI("https://www.airport-data.com/api/ac_thumb.json") // @TODO DI

    init {
        this.state.value = State.HIDDEN
    }

    fun getImage(icao: String?) {
        if (icao != null) {
            viewModelScope.launch {
                try {
                    this@AircraftInfoViewModel.image.value = this@AircraftInfoViewModel.api.fetch(icao)
                } catch (e: Exception) {

                    this@AircraftInfoViewModel._error.value = Event(
                        e.message
                            ?: getApplication<Application>().applicationContext.getString(R.string.error_during_loading_aircraft_image)
                    )
                }
            }
        }
    }

    enum class State {
        HIDDEN,
        PEEKING,
        OPEN
    }
}