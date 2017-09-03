package cz.adsb.czadsb

import com.github.kittinunf.fuel.Fuel
import cz.adsb.czadsb.model.AircraftList
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object PlanesFetcher {
    private val URL = "https://czadsb.cz/private/AircraftList.json"

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
        trustEveryone()
        val (_, _, result) = Fuel.post("$URL?fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
            .responseObject(AircraftList.Deserializer())
        val (aircraftList, err) = result
        return aircraftList
    }

    private fun fetchChanges(ldv: String, north: Double, south: Double, west: Double, east: Double): AircraftList? {
        trustEveryone()
        val (_, _, result) = Fuel.post("$URL?ldv=$ldv&fNbnd=$north&fSBnd=$south&fWbnd=$west&fEBnd=$east")
                .responseObject(AircraftList.Deserializer())
        val (aircraftList, err) = result
        return aircraftList
    }

    private fun trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier { hostname, session -> true }
            val context = SSLContext.getInstance("TLS")
            context.init(null, arrayOf<X509TrustManager>(object: X509TrustManager {
                override fun checkClientTrusted(chain:Array<X509Certificate>,
                                       authType:String) {}
                override fun checkServerTrusted(chain:Array<X509Certificate>,
                                       authType:String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate?> =
                        arrayOfNulls<X509Certificate>(0)
            }), SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory())
        }
        catch (e:Exception) { // should never happen
            e.printStackTrace()
        }
    }
}