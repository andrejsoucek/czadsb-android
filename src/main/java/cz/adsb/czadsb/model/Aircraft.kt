package cz.adsb.czadsb.model

import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint

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
    var lat: Number?,
    @SerializedName("Long")
    var long: Number?,
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
    val position: GeoPoint?
        get() = if (willShowOnMap()) GeoPoint(lat!!.toDouble(), long!!.toDouble(), amslAlt!!.toDouble()) else null

    val iconName: String
        get() {
            if (military == true && engineType == EngineTypeEnum.JET.eT && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 4){
                return IconTypeEnum.WTC_HEAVY_MIL_4_JET.iconName
            }

            if (military == true && engineType == EngineTypeEnum.TURBO.eT && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 4){
                return IconTypeEnum.TURBO_PROP_MIL_4.iconName
            }

            if (military == true && engineType == EngineTypeEnum.JET.eT && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 2){
                return IconTypeEnum.WTC_HEAWY_MIL_2_JET.iconName
            }

            if (military == true && engineType == EngineTypeEnum.TURBO.eT && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 2){
                return IconTypeEnum.TURBO_PROP_MIL_2.iconName
            }

            if (military == true && species == SpeciesEnum.HELICOPTER.spec){
                return IconTypeEnum.HELICOPTER_MILITARY.iconName
            }

            if (type == "RADAR"){
                return IconTypeEnum.RADAR.iconName
            }

            if (military == true && engineType == EngineTypeEnum.JET.eT && wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turb && engines?.toInt() == 1) {
                return IconTypeEnum.F16.iconName
            }

            if (squawk == "0045" || squawk == "0046" || squawk == "0047" || squawk == "0020"){
                return IconTypeEnum.HELICOPTER_MEDICAL.iconName
            }

            if (type == "CLEAN" || type == "CLEANER" || type == "TUG" || type == "SQB"){
                return IconTypeEnum.CAR_FOLLOW_ME.iconName
            }

            if (type == "SAFETY CAR" || type == "FLLME" || type == "ELECTRIC" || type == "BIRD" || type == "AD REPAIR" || type == "CAR"){
                return IconTypeEnum.CAR.iconName
            }

            if (type == "FIRE"){
                return IconTypeEnum.CAR_FIRE.iconName
            }

            if (type == "UFO"){
                return IconTypeEnum.CAR_FIRE.iconName
            }

            if (species == SpeciesEnum.GROUND_VEHICLE.spec){
                return IconTypeEnum.GROUND_VEHICLE.iconName
            }

            if (species == SpeciesEnum.TOWER.spec){
                return IconTypeEnum.TOWER.iconName
            }

            if (species == SpeciesEnum.HELICOPTER.spec){
                return IconTypeEnum.HELICOPTER.iconName
            }

            if (type == "GLID"){
                return IconTypeEnum.GLIDER.iconName
            }

            if (type == "A388"){
                return IconTypeEnum.A380.iconName
            }

            if (type == "E6" || (type?.length == 4 && type!!.startsWith("A34", true))){
                return IconTypeEnum.A340.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Light.turb && engineType != EngineTypeEnum.JET.eT && engines?.toInt() == 1){
                return IconTypeEnum.WTC_LIGHT_1_PROP.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Light.turb && engineType != EngineTypeEnum.JET.eT){
                return IconTypeEnum.WTC_LIGHT_2_PROP.iconName
            }

            if (engineType == EngineTypeEnum.JET.eT &&
                    (wakeTurbulenceCat == WakeTurbulenceEnum.Light.turb ||
                            (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turb && engineMount == EnginePlacementEnum.AFT_MOUNTED.placement))){
                return IconTypeEnum.GLFX.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turb && engines?.toInt() == 4 && engineType == EngineTypeEnum.JET.eT){
                return IconTypeEnum.WTC_MEDIUM_4_JET.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turb && engines?.toInt() != 4 && engineType == EngineTypeEnum.JET.eT){
                return IconTypeEnum.WTC_MEDIUM_2_JET.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turb && engines?.toInt() != 4){
                return IconTypeEnum.WTC_MEDIUM_2_TURBO.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Heavy.turb && engines?.toInt() == 4){
                return IconTypeEnum.WTC_HEAVY_4_JET.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Heavy.turb && engines?.toInt() != 4){
                return IconTypeEnum.WTC_HEAVY_2_JET.iconName
            }

            if (engines?.toInt() == 4 && engineType == EngineTypeEnum.TURBO.eT){
                return IconTypeEnum.TURBO_PROP_4.iconName
            }

            if (type == "BALL"){
                return IconTypeEnum.BALL.iconName
            }

            return IconTypeEnum.AIRPLANE.iconName
        }

    fun willShowOnMap(): Boolean = this.lat !== null && this.long !== null && this.amslAlt !== null && this.hdg !== null

    override fun equals(other: Any?): Boolean = other is Aircraft && this.id === other.id
    override fun hashCode(): Int = id.hashCode()
}
