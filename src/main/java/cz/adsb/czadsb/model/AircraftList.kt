package cz.adsb.czadsb.model

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class AircraftList(
    var src: Number = 0,
    var feeds: Array<Feed> = emptyArray(),
    var srcFeed: Number = 0,
    var showSil: Boolean = false,
    var showFlg: Boolean = false,
    var showPic: Boolean = false,
    var flgH: Number = 0,
    var flgW: Number = 0,
    var acList: Array<Aircraft> = emptyArray(),
    var totalAc: Number = 0,
    var lastDv: String = "",
    var shtTrlSec: Number = 0,
    var stm: Number = 0
    ) {

    val aircrafts: MutableMap<Number, Aircraft>
        get() {
            val map: MutableMap<Number, Aircraft> = mutableMapOf()
            if (acList.isNotEmpty()) {
                acList.forEach {
                    map.put(it.id, it)
                }
            }
            return map
        }

    class Deserializer : ResponseDeserializable<AircraftList> {
        override fun deserialize(content: String) = Gson().fromJson(content, AircraftList::class.java)
    }
}