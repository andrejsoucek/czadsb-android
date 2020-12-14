package cz.adsb.czadsb.model.images

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("image") val imageUrl: String,
    @SerializedName("link") val pageUrl: String,
    @SerializedName("photographer") val author: String,
)