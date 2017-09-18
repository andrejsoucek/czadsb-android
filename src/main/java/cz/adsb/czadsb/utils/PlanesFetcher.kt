package cz.adsb.czadsb.utils

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.model.AircraftList

object PlanesFetcher {

    fun fetchAircrafts(ctx: Context, aircraftList: AircraftList, north: Double, south: Double, west: Double, east: Double): AircraftList {
        return if (aircraftList.lastDv === "") {
            fetchFull(resolveUrl(ctx), north, south, west, east)
        } else {
            fetchChanges(resolveUrl(ctx), aircraftList.lastDv, north, south, west, east)
        }
    }

    private fun fetchFull(url: String, north: Double, south: Double, west: Double, east: Double): AircraftList {
        val (_, _, result) = Fuel.post("$url?fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
            .responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList?: AircraftList()
    }

    private fun fetchChanges(url: String, ldv: String, north: Double, south: Double, west: Double, east: Double): AircraftList {
        val (_, _, result) = Fuel.post("$url?ldv=$ldv&fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
                .responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList?: AircraftList()
    }

    private fun resolveUrl(ctx: Context): String = ctx.getProperty("url_public")
}