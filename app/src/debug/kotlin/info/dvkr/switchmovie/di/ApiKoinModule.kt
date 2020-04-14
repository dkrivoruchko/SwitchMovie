package info.dvkr.switchmovie.di

import android.net.TrafficStats
import info.dvkr.switchmovie.data.repository.api.MovieApiService
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val apiKoinModule = module {

    single {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        OkHttpClient().newBuilder()
            .certificatePinner(
                CertificatePinner.Builder()
                    .add(getProperty("API_BASE_DOMAIN"), getProperty("API_BASE_DOMAIN_CERT_HASH"))
                    .build()
            )
            .addInterceptor { chain ->
                TrafficStats.setThreadStatsTag(50); chain.proceed(chain.request())
            }
            .addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
            })
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