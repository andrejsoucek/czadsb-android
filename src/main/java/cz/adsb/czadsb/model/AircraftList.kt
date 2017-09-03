package cz.adsb.czadsb.model

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class AircraftList(var acList: Array<Aircraft>, var totalAc: Int, var lastDv: String) {

    class Deserializer : ResponseDeserializable<AircraftList> {
        override fun deserialize(content: String) = Gson().fromJson(content, AircraftList::class.java)
    }
}