package cz.adsb.czadsb.model.planes

import com.google.gson.annotations.SerializedName
import org.osmdroid.util.GeoPoint

data class Aircraft(
    @SerializedName("Id") val id: Number,
    @SerializedName("TSecs") val secondsTracked: Number,
    @SerializedName("Rcvr") val receiverId: Number,
    @SerializedName("HasSig") val hasSignal: Boolean?,
    @SerializedName("Icao") val icao: String?,
    @SerializedName("Bad") val invalidIcao: Boolean?,
    @SerializedName("Reg") val registration: String?,
    @SerializedName("FSeen") val fSeen: String?,
    @SerializedName("CMsgs") val messagesReceived: Number?,
    @SerializedName("Alt") val stPressureAlt: Number?,
    @SerializedName("GAlt") val amslAlt: Number?,
    @SerializedName("InHg") val inHg: Number?,
    @SerializedName("AltT") val AltType: Number?,
    @SerializedName("Call") val callsign: String?,
    @SerializedName("Lat") val lat: Number?,
    @SerializedName("Long") val long: Number?,
    @SerializedName("PosTime") val lastReported: Number?,
    @SerializedName("Mlat") val mlat: Boolean?,
    @SerializedName("Tisb") val tisb: Boolean?,
    @SerializedName("Spd") val spd: Number?,
    @SerializedName("Trak") val hdg: Number?,
    @SerializedName("TrkH") val trkH: Boolean?,
    @SerializedName("Type") val type: String?,
    @SerializedName("Mdl") val model: String?,
    @SerializedName("Man") val manufacturer: String?,
    @SerializedName("CNum") val serialNumber: String?,
    @SerializedName("From") val from: String?,
    @SerializedName("To") val to: String?,
    @SerializedName("Op") val operator: String?,
    @SerializedName("OpIcao") val operatorIcao: String?,
    @SerializedName("Sqk") val squawk: String?,
    @SerializedName("Help") val emergency: Boolean?,
    @SerializedName("Vsi") val vSpd: Number?,
    @SerializedName("VsiT") val vSpdType: Number?,
    @SerializedName("WTC") val wakeTurbulenceCat: Int?,
    @SerializedName("Species") val species: Int?,
    @SerializedName("Engines") val engines: String?,
    @SerializedName("EngType") val engineType: Int?,
    @SerializedName("EngMount") val engineMount: Int?,
    @SerializedName("Mil") val military: Boolean?,
    @SerializedName("Cou") val country: String?,
    @SerializedName("HasPic") val hasPic: Boolean?,
    @SerializedName("Interested") val interesting: Boolean?,
    @SerializedName("FlightsCount") val flightsCount: Number?,
    @SerializedName("Gnd") val onGround: Boolean?,
    @SerializedName("SpdTyp") val speedType: Number?,
    @SerializedName("CallSus") val invalidCallsign: Boolean?,
    @SerializedName("Trt") val transponderType: Number?,
    @SerializedName("Year") val manufactured: String?
) {
    val position: GeoPoint?
        get() = if (willShowOnMap()) {
            GeoPoint(lat!!.toDouble(), long!!.toDouble(), amslAlt!!.toDouble())
        } else null

    val iconName: String
        get() {
            if (military == true && engineType == EngineTypeEnum.JET.type && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 4) {
                return IconTypeEnum.WTC_HEAVY_MIL_4_JET.iconName
            }

            if (military == true && engineType == EngineTypeEnum.TURBO.type && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 4) {
                return IconTypeEnum.TURBO_PROP_MIL_4.iconName
            }

            if (military == true && engineType == EngineTypeEnum.JET.type && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 2) {
                return IconTypeEnum.WTC_HEAWY_MIL_2_JET.iconName
            }

            if (military == true && engineType == EngineTypeEnum.TURBO.type && species != SpeciesEnum.HELICOPTER.spec && engines?.toInt() == 2) {
                return IconTypeEnum.TURBO_PROP_MIL_2.iconName
            }

            if (military == true && species == SpeciesEnum.HELICOPTER.spec) {
                return IconTypeEnum.HELICOPTER_MILITARY.iconName
            }

            if (type == "RADAR") {
                return IconTypeEnum.RADAR.iconName
            }

            if (military == true && engineType == EngineTypeEnum.JET.type && wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turbulence && engines?.toInt() == 1) {
                return IconTypeEnum.F16.iconName
            }

            if (squawk == "0045" || squawk == "0046" || squawk == "0047" || squawk == "0020") {
                return IconTypeEnum.HELICOPTER_MEDICAL.iconName
            }

            if (type == "FLLME") {
                return IconTypeEnum.CAR_FOLLOW_ME.iconName
            }

            if (type == "CLEAN" || type == "CLEANER" || type == "TUG" || type == "SQB" || type == "SAFETY CAR" || type == "ELECTRIC" || type == "BIRD" || type == "AD REPAIR" || type == "CAR") {
                return IconTypeEnum.CAR.iconName
            }

            if (type == "FIRE") {
                return IconTypeEnum.CAR_FIRE.iconName
            }

            if (type == "UFO") {
                return IconTypeEnum.CAR_FIRE.iconName
            }

            if (species == SpeciesEnum.GROUND_VEHICLE.spec) {
                return IconTypeEnum.GROUND_VEHICLE.iconName
            }

            if (species == SpeciesEnum.TOWER.spec) {
                return IconTypeEnum.TOWER.iconName
            }

            if (species == SpeciesEnum.HELICOPTER.spec) {
                return IconTypeEnum.HELICOPTER.iconName
            }

            if (type == "GLID") {
                return IconTypeEnum.GLIDER.iconName
            }

            if (type == "A388") {
                return IconTypeEnum.A380.iconName
            }

            if (type == "E6" || (type != null && type.length == 4 && type.startsWith(
                    "A34",
                    true
                ))
            ) {
                return IconTypeEnum.A340.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Light.turbulence && engineType != EngineTypeEnum.JET.type && engines?.toInt() == 1) {
                return IconTypeEnum.WTC_LIGHT_1_PROP.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Light.turbulence && engineType != EngineTypeEnum.JET.type) {
                return IconTypeEnum.WTC_LIGHT_2_PROP.iconName
            }

            if (engineType == EngineTypeEnum.JET.type &&
                (wakeTurbulenceCat == WakeTurbulenceEnum.Light.turbulence ||
                        (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turbulence && engineMount == EnginePlacementEnum.AFT_MOUNTED.placement))
            ) {
                return IconTypeEnum.GLFX.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turbulence && engines?.toInt() == 4 && engineType == EngineTypeEnum.JET.type) {
                return IconTypeEnum.WTC_MEDIUM_4_JET.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turbulence && engines?.toInt() != 4 && engineType == EngineTypeEnum.JET.type) {
                return IconTypeEnum.WTC_MEDIUM_2_JET.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Medium.turbulence && engines?.toInt() != 4) {
                return IconTypeEnum.WTC_MEDIUM_2_TURBO.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Heavy.turbulence && engines?.toInt() == 4) {
                return IconTypeEnum.WTC_HEAVY_4_JET.iconName
            }

            if (wakeTurbulenceCat == WakeTurbulenceEnum.Heavy.turbulence && engines?.toInt() != 4) {
                return IconTypeEnum.WTC_HEAVY_2_JET.iconName
            }

            if (engines?.toInt() == 4 && engineType == EngineTypeEnum.TURBO.type) {
                return IconTypeEnum.TURBO_PROP_4.iconName
            }

            if (type == "BALL") {
                return IconTypeEnum.BALL.iconName
            }

            return IconTypeEnum.AIRPLANE.iconName
        }

    fun willShowOnMap(): Boolean =
        this.lat != null && this.long != null && this.amslAlt != null && this.hdg != null

    override fun equals(other: Any?): Boolean = other is Aircraft && this.id == other.id
    override fun hashCode(): Int = id.hashCode()
}
