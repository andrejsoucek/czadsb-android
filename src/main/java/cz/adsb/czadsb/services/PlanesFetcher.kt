package cz.adsb.czadsb.services

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.model.AircraftList
import cz.adsb.czadsb.model.User
import cz.adsb.czadsb.utils.getProperty

object PlanesFetcher {

    fun fetch(ctx: Context, aircraftList: AircraftList, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        return if (aircraftList.lastDv == "") {
            fetchFull(ctx, north, south, west, east)
        } else {
            fetchChanges(ctx, aircraftList.lastDv, north, south, west, east)
        }
    }

    private fun fetchFull(ctx: Context, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        val request = Fuel.post("${resolveUrl(ctx)}?fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
            .appendHeader("Authorization", "Bearer ${User.getIdentity(ctx)}")
        val (_, _, result) = request.responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList ?: AircraftList()
    }

    private fun fetchChanges(ctx: Context, ldv: String, north: Double, south: Double, west: Double, east: Double): AircraftList
    {
        val request = Fuel.post("${resolveUrl(ctx)}?ldv=$ldv&fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
            .appendHeader("Authorization", "Bearer ${User.getIdentity(ctx)}")
        val (_, _, result) = request.responseObject(AircraftList.Deserializer())
        val (aircraftList, _) = result
        return aircraftList ?: AircraftList()
    }

    private fun resolveUrl(ctx: Context): String = ctx.getProperty("aircraftlist_url")
}