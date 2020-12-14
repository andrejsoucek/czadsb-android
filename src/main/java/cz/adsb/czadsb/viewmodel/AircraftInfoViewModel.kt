package cz.adsb.czadsb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import cz.adsb.czadsb.model.planes.Aircraft
import org.osmdroid.util.GeoPoint

class AircraftInfoViewModel(application: Application) : AndroidViewModel(application) {

    val state = MutableLiveData<State>()

    val selectedAircraft = MutableLiveData<Aircraft>()

    val track = MutableLiveData<List<GeoPoint>>()

    init {
        this.state.value = State.HIDDEN
    }

    enum class State {
        HIDDEN,
        PEEKING,
        OPEN
    }
}