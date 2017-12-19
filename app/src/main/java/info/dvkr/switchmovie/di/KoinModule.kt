package info.dvkr.switchmovie.di

import android.net.TrafficStats
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.switchmovie.data.movie.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocal
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.notifications.NotificationManagerImpl
import info.dvkr.switchmovie.data.presenter.PresenterFactory
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.notifications.NotificationManager
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.UseCases
import info.dvkr.switchmovie.domain.usecase.UseCasesImpl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.Executors


class KoinModule : AndroidModule() {
  companion object {
    const val MOVIE_REPOSITORY_CONTEXT = "MOVIE_REPOSITORY_CONTEXT"
  }

  override fun context() = applicationContext {
    provide { NotificationManagerImpl() } bind (NotificationManager::class)

    provide {
      PresenterFactory(get(), get())
    } bind (PresenterFactory::class)

    provide {
      SettingsImpl(
          BinaryPreferencesBuilder(androidApplication)
              .name("AppSettings")
              .exceptionHandler { Timber.e(it) }
              .build()
      )
    } bind (Settings::class)

    provide {
      OkHttpClient().newBuilder()
          .addInterceptor(Interceptor { chain ->
            TrafficStats.setThreadStatsTag(50)
            chain.proceed(chain.request())
          })
          .addInterceptor(
              HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
          .build()
    } bind (OkHttpClient::class)

    provide {
      Retrofit.Builder()
          .baseUrl(BuildConfig.BASE_API_URL)
          .addConverterFactory(MoshiConverterFactory.create())
          .callbackExecutor(Executors.newSingleThreadExecutor())
          .client(get())
          .build()
          .create(MovieApi.Service::class.java)
    } bind (MovieApi.Service::class)

    provide { MovieApiService(get(), BuildConfig.API_KEY) }

    provide {
      MovieLocalService(
          get(),
          BinaryPreferencesBuilder(androidApplication)
              .name("AppCache")
              .registerPersistable(MovieLocal.LocalMovie.LOCAL_MOVIE_KEY, MovieLocal.LocalMovie::class.java)
              .registerPersistable(MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList::class.java)
              .exceptionHandler { Timber.e(it) }
              .build()
      )
    } bind (MovieLocalService::class)

    provide {
      MovieRepositoryImpl(get(), get())
    } bind (MovieRepository::class)

    provide {
      UseCasesImpl(get())
    } bind (UseCases::class)

  }
}