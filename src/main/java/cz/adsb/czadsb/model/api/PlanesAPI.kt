package cz.adsb.czadsb.model.api

import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.model.planes.AircraftList
import cz.adsb.czadsb.model.user.Authenticator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlanesAPI(
    private val authenticator: Authenticator,
    private val url: String,
) {

    suspend fun fetch(lastDv: String, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        return withContext(Dispatchers.IO) {
            if (lastDv == "") {
                fetchFull(north, south, west, east)
            } else {
                fetchChanges(lastDv, north, south, west, east)
            }
        }
    }

    private fun fetchFull(north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        val request = Fuel.post("${this.url}?fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
        val (_, _, result) = request.responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList ?: AircraftList()
    }

    private fun fetchChanges(ldv: String, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        val request = Fuel.post("${this.url}?ldv=$ldv&fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
        val (_, _, result) = request.responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList ?: AircraftList()
    }
}