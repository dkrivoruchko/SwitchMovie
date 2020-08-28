package info.dvkr.switchmovie.di

import android.net.TrafficStats
import info.dvkr.switchmovie.BuildConfig
import info.dvkr.switchmovie.data.repository.api.MovieApiService
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val apiKoinModule = module {

    single {
        OkHttpClient().newBuilder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add(BuildConfig.API_BASE_DOMAIN, BuildConfig.API_BASE_DOMAIN_CERT_HASH)
                    .build()
            )
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor { chain -> TrafficStats.setThreadStatsTag(50); chain.proceed(chain.request()) }
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
                }
            }
            .build()
    }

    single {
        MovieApiService(
            get(KoinQualifier.IO_COROUTINE_SCOPE),
            get(),
            BuildConfig.API_BASE_URL,
            BuildConfig.API_KEY,
            BuildConfig.API_BASE_IMAGE_URL
        )
    }
}