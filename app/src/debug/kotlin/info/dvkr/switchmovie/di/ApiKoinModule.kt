package info.dvkr.switchmovie.di

import android.net.TrafficStats
import info.dvkr.switchmovie.data.repository.api.MovieApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val apiKoinModule = module {

    val cacheSize: Long = 10 * 1024 * 1024

    single {
        OkHttpClient().newBuilder()
            .cache(Cache(androidContext().applicationContext.cacheDir, cacheSize))
            .addInterceptor { chain ->
                TrafficStats.setThreadStatsTag(50); chain.proceed(chain.request())
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
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