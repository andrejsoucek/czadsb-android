package cz.adsb.czadsb.model

import com.google.gson.annotations.SerializedName
import org.osmdroid.views.overlay.Marker

data class Aircraft(
    @SerializedName("Id")
    var id: Number,
    @SerializedName("TSecs")
    var secondsTracked: Number,
    @SerializedName("Rcvr")
    var receiverId: Number,
    @SerializedName("HasSig")
    var hasSignal: Boolean?,
    @SerializedName("Icao")
    var icao: String?,
    @SerializedName("Bad")
    var invalidIcao: Boolean?,
    @SerializedName("Reg")
    var registration: String?,
    @SerializedName("FSeen")
    var fSeen: String?,
    @SerializedName("CMsgs")
    var messagesReceived: Number?,
    @SerializedName("Alt")
    var stPressureAlt: Number?,
    @SerializedName("GAlt")
    var amslAlt: Number?,
    @SerializedName("InHg")
    var inHg: Number?,
    @SerializedName("AltT")
    var AltType: Number?,
    @SerializedName("Call")
    var callsign: String?,
    @SerializedName("Lat")
    var lat: Float?,
    @SerializedName("Long")
    var long: Float?,
    @SerializedName("PosTime")
    var lastReported: Number?,
    @SerializedName("Mlat")
    var mlat: Boolean?,
    @SerializedName("Tisb")
    var tisb: Boolean?,
    @SerializedName("Spd")
    var spd: Number?,
    @SerializedName("Trak")
    var hdg: Number?,
    @SerializedName("TrkH")
    var trkH: Boolean?,
    @SerializedName("Type")
    var type: String?,
    @SerializedName("Mdl")
    var model: String?,
    @SerializedName("Man")
    var manufacturer: String?,
    @SerializedName("CNum")
    var serialNumber: String?,
    @SerializedName("From")
    var from: String?,
    @SerializedName("To")
    var to: String?,
    @SerializedName("Op")
    var operator: String?,
    @SerializedName("OpIcao")
    var operatorIcao: String?,
    @SerializedName("Sqk")
    var squawk: String?,
    @SerializedName("Help")
    var emergency: Boolean?,
    @SerializedName("Vsi")
    var vSpd: Number?,
    @SerializedName("VsiT")
    var vSpdType: Number?,
    @SerializedName("WTC")
    var wakeTurbulenceCat: Number?,
    @SerializedName("Species")
    var species: Number?,
    @SerializedName("Engines")
    var engines: String?,
    @SerializedName("EngType")
    var engineType: Number?,
    @SerializedName("EngMount")
    var engineMount: Number?,
    @SerializedName("Mil")
    var military: Boolean?,
    @SerializedName("Cou")
    var country: String?,
    @SerializedName("HasPic")
    var hasPic: Boolean?,
    @SerializedName("Interested")
    var interesting: Boolean?,
    @SerializedName("FlightsCount")
    var flightsCount: Number?,
    @SerializedName("Gnd")
    var onGround: Boolean?,
    @SerializedName("SpdTyp")
    var speedType: Number?,
    @SerializedName("CallSus")
    var invalidCallsign: Boolean?,
    @SerializedName("Trt")
    var transponderType: Number?,
    @SerializedName("Year")
    var manufactured: String?
) {
    override fun equals(other: Any?): Boolean = other is Aircraft && this.id === other.id
    override fun hashCode(): Int = id.hashCode()
}
