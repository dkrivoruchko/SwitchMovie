package info.dvkr.switchmovie.di

import android.net.TrafficStats
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.switchmovie.data.movie.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocal
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.presenter.ViewModelFactory
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.repository.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.Executors


class KoinModule : AndroidModule() {
    override fun context() = applicationContext {

        provide { ViewModelFactory(get()) } bind (ViewModelFactory::class)

        provide {
            SettingsImpl(
                    BinaryPreferencesBuilder(androidApplication)
                            .name("AppSettings")
                            .exceptionHandler { Timber.e(it) }
                            .build()
            )
        } bind (Settings::class)

        provide {
            createRetrofitInstance()
        } bind (MovieApi.Service::class)

        provide { MovieApiService(get(), BuildConfig.API_KEY) }

        provide {
            MovieLocalService(
                    BinaryPreferencesBuilder(androidApplication)
                            .name("AppCache")
                            .registerPersistable(MovieLocal.LocalMovie.LOCAL_MOVIE_KEY, MovieLocal.LocalMovie::class.java)
                            .registerPersistable(MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList::class.java)
                            .exceptionHandler { Timber.e(it) }
                            .build()
            )
        } bind (MovieLocalService::class)

        provide { MovieRepositoryImpl(get(), get()) } bind (MovieRepository::class)
    }

    private fun createRetrofitInstance(): MovieApi.Service {
        var client = OkHttpClient.Builder().addInterceptor(TrafficStatInterceptor()).build()

        return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_API_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .callbackExecutor(Executors.newSingleThreadExecutor())
                .build()
                .create(MovieApi.Service::class.java)
    }

    private class TrafficStatInterceptor : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            TrafficStats.setThreadStatsTag(50)
            return chain.proceed(chain.request())
        }
    }
}