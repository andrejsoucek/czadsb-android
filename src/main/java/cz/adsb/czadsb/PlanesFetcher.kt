package cz.adsb.czadsb

import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.model.AircraftList

object PlanesFetcher {
    private val URL = "https://czadsb.cz/public/AircraftList.json"

    fun fetchAircrafts(north: Double, south: Double, west: Double, east: Double): AircraftList {
        var aircraftList: AircraftList? = null
        if (aircraftList?.lastDv != null) {
            aircraftList = fetchChanges(aircraftList.lastDv, north, south, west, east)
            return aircraftList!!
        } else {
            aircraftList = fetchFull(north, south, west, east)
            return aircraftList!!
        }
    }

    private fun fetchFull(north: Double, south: Double, west: Double, east: Double): AircraftList? {
        val (_, _, result) = Fuel.post("$URL?fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
            .responseObject(AircraftList.Deserializer())
        val (aircraftList, err) = result
        return aircraftList
    }

    private fun fetchChanges(ldv: String, north: Double, south: Double, west: Double, east: Double): AircraftList? {
        val (_, _, result) = Fuel.post("$URL?ldv=$ldv&fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
                .responseObject(AircraftList.Deserializer())
        val (aircraftList, err) = result
        return aircraftList
    }
}