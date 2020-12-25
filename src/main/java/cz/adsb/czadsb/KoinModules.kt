package cz.adsb.czadsb

import android.content.Context
import com.auth0.android.Auth0
import cz.adsb.czadsb.model.api.ImagesAPI
import cz.adsb.czadsb.model.api.PlanesAPI
import cz.adsb.czadsb.model.user.Authenticator
import cz.adsb.czadsb.utils.getProperty
import cz.adsb.czadsb.viewmodel.AircraftInfoViewModel
import cz.adsb.czadsb.viewmodel.AircraftListViewModel
import cz.adsb.czadsb.viewmodel.UserViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        UserViewModel(get(), get())
    }
    viewModel {
        AircraftInfoViewModel(get(), get())
    }
    viewModel {
        AircraftListViewModel(get(), get())
    }
}

val apiModule = module {
    fun provideImagesAPI(): ImagesAPI {
        return ImagesAPI("https://www.airport-data.com/api/ac_thumb.json")
    }

    fun providePlanesAPI(authenticator: Authenticator, url: String): PlanesAPI {
        return PlanesAPI(authenticator, url)
    }

    single {
        provideImagesAPI()
    }
    single {
        providePlanesAPI(get(), this.androidApplication().applicationContext.getProperty("aircraftlist_url"))
    }
}

val authenticationModule = module {
    fun provideAuth0(ctx: Context): Auth0 {
        val auth0 = Auth0(ctx)
        auth0.isOIDCConformant = true

        return auth0
    }

    single {
        provideAuth0(get())
    }
    single {
        Authenticator(get(), get())
    }
}