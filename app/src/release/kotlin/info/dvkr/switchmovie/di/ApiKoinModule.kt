package info.dvkr.switchmovie.di

import com.datatheorem.android.trustkit.TrustKit
import info.dvkr.switchmovie.data.repository.api.MovieApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val apiKoinModule = module {

    val cacheSize: Long = 10 * 1024 * 1024

    single {
        OkHttpClient().newBuilder()
            .cache(Cache(androidContext().applicationContext.cacheDir, cacheSize))
            .sslSocketFactory(
                TrustKit.getInstance().getSSLSocketFactory(getProperty("API_BASE_DOMAIN")),
                TrustKit.getInstance().getTrustManager(getProperty("API_BASE_DOMAIN"))
            )
            .build()
    }

    single {
        MovieApiService(
            get(),
            getProperty("API_BASE_URL"),
            getProperty("API_KEY"),
            getProperty("API_BASE_IMAGE_URL")
        )
    }
}