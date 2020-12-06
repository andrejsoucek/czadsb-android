package cz.adsb.czadsb.services

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.model.AircraftList
import cz.adsb.czadsb.model.User
import cz.adsb.czadsb.utils.getProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlanesFetcher {

    suspend fun fetch(ctx: Context, lastDv: String, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        return withContext(Dispatchers.IO) {
            if (lastDv == "") {
                fetchFull(ctx, north, south, west, east)
            } else {
                fetchChanges(ctx, lastDv, north, south, west, east)
            }
        }
    }

    private fun fetchFull(ctx: Context, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        val request = Fuel.post("${resolveUrl(ctx)}?fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
        val (_, _, result) = request.responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList ?: AircraftList()
    }

    private fun fetchChanges(ctx: Context, ldv: String, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        val request = Fuel.post("${resolveUrl(ctx)}?ldv=$ldv&fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
        val (_, _, result) = request.responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList ?: AircraftList()
    }

    private fun resolveUrl(ctx: Context): String = ctx.getProperty("aircraftlist_url")
}