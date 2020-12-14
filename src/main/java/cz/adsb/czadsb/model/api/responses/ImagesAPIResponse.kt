package cz.adsb.czadsb.model.api.responses

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import cz.adsb.czadsb.model.images.Image

data class ImagesAPIResponse(
    @SerializedName("status") val status: Number,
    @SerializedName("count") val count: Number?,
    @SerializedName("data") val images: Array<Image?>?,
    @SerializedName("error") val error: String?,
) {

    class Deserializer : ResponseDeserializable<ImagesAPIResponse> {
        override fun deserialize(content: String): ImagesAPIResponse =
            Gson().fromJson(content, ImagesAPIResponse::class.java)
    }
}