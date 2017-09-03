package cz.adsb.czadsb.model

import com.google.gson.annotations.SerializedName

data class Aircraft(
        @SerializedName("Id")
        var id: Number,
        @SerializedName("Icao")
        var icao: String,
        @SerializedName("Call")
        var callsign: String?,
        @SerializedName("Reg")
        var registration: String?,
        @SerializedName("Lat")
        var lat: Float?,
        @SerializedName("Long")
        var long: Float?,
        @SerializedName("GAlt")
        var alt: Number?,
        @SerializedName("Spd")
        var spd: Number?,
        @SerializedName("Trak")
        var hdg: Number?,
        @SerializedName("Vsi")
        var verticalSpd: Number?,
        @SerializedName("From")
        var from: String?,
        @SerializedName("To")
        var to: String?,
        @SerializedName("Stops")
        var stops: Array<String> = emptyArray()
)