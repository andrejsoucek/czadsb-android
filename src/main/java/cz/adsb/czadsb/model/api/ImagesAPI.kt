package cz.adsb.czadsb.model.api

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import cz.adsb.czadsb.model.api.responses.ImagesAPIResponse
import cz.adsb.czadsb.model.images.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImagesAPI(
    private val url: String,
) {

    suspend fun fetch(icao: String): Image? {
        return withContext(Dispatchers.IO) {
            val request = Fuel.post("${this@ImagesAPI.url}?m=$icao")
            val (_, _, result) = request.responseObject(ImagesAPIResponse.Deserializer())
            when (result) {
                is Result.Success -> {
                    result.get().images?.get(0)
                }
                is Result.Failure -> {
                    throw result.getException()
                }
            }
        }
    }
}