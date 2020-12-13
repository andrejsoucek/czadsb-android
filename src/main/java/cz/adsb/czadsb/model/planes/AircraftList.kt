package cz.adsb.czadsb.model.planes

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class AircraftList(
    val src: Number = 0,
    val feeds: Array<Feed> = emptyArray(),
    val srcFeed: Number = 0,
    val showSil: Boolean = false,
    val showFlg: Boolean = false,
    val showPic: Boolean = false,
    val flgH: Number = 0,
    val flgW: Number = 0,
    val acList: Array<Aircraft> = emptyArray(),
    val totalAc: Number = 0,
    val lastDv: String = "",
    val shtTrlSec: Number = 0,
    val stm: Number = 0
    ) {

    val aircrafts: MutableMap<Number, Aircraft>
        get() {
            val map: MutableMap<Number, Aircraft> = mutableMapOf()
            if (acList.isNotEmpty()) {
                acList.forEach {
                    map[it.id] = it
                }
            }
            return map
        }

    class Deserializer : ResponseDeserializable<AircraftList> {
        override fun deserialize(content: String): AircraftList = Gson().fromJson(content, AircraftList::class.java)
    }

    override fun equals(other: Any?): Boolean {
        return other is AircraftList && other.src == src && other.lastDv == lastDv
    }

    override fun hashCode(): Int {
        return lastDv.toInt()
    }
}