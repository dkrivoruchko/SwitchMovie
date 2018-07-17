package info.dvkr.switchmovie.di

import android.net.TrafficStats
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import info.dvkr.switchmovie.data.helpers.LoggerImpl
import info.dvkr.switchmovie.data.movie.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocal
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.notifications.NotificationManager
import info.dvkr.switchmovie.data.notifications.NotificationManagerImpl
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailPresenter
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridPresenter
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.helpers.Logger
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.UseCases
import info.dvkr.switchmovie.domain.usecase.UseCasesImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.architecture.ext.viewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.applicationContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


val baseKoinModule = applicationContext {

    bean { NotificationManagerImpl() as NotificationManager }

    bean {
        SettingsImpl(
            BinaryPreferencesBuilder(androidApplication())
                .name("AppSettings")
                .exceptionHandler { Timber.e(it) }
                .build()
        ) as Settings
    }

    bean {
        OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain ->
                TrafficStats.setThreadStatsTag(50)
                chain.proceed(chain.request())
            })
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    bean {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_API_URL)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                )
            )
            .callbackExecutor(Executors.newSingleThreadExecutor())
            .client(get())
            .build()
            .create(MovieApi.Service::class.java) as MovieApi.Service
    }

    bean { MovieApiService(get(), BuildConfig.API_KEY) }

    bean {
        MovieLocalService(
            get(),
            BinaryPreferencesBuilder(androidApplication())
                .name("AppCache")
                .registerPersistable(
                    MovieLocal.LocalMovie.LOCAL_MOVIE_KEY, MovieLocal.LocalMovie::class.java
                )
                .registerPersistable(
                    MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList::class.java
                )
                .exceptionHandler { Timber.e(it) }
                .build()
        )
    }


    // Logger
    bean { LoggerImpl() as Logger }

    bean { MovieRepositoryImpl(get(), get()) as MovieRepository.RW } bind MovieRepository.RO::class

    bean { UseCasesImpl(get(), get()) as UseCases }

    viewModel { MovieGridPresenter(get(), get()) }

    viewModel { MovieDetailPresenter(get(), get()) }
}