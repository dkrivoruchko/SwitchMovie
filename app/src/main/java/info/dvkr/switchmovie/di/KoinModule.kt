package info.dvkr.switchmovie.di

import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.switchmovie.data.movie.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocal
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.presenter.PresenterFactory
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.repository.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.newSingleThreadContext
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber


class KoinModule : AndroidModule() {
    companion object {
        const val PRESENTER_CONTEXT = "PresenterActorContext"
        const val REPOSITORY_CONTEXT = "RepositoryActorContext"
    }

    override fun context() = applicationContext {

        provide(PRESENTER_CONTEXT) { newSingleThreadContext(PRESENTER_CONTEXT) } bind (ThreadPoolDispatcher::class)

        provide(REPOSITORY_CONTEXT) { newSingleThreadContext(REPOSITORY_CONTEXT) } bind (ThreadPoolDispatcher::class)

        provide { PresenterFactory(get(PRESENTER_CONTEXT), get()) } bind (PresenterFactory::class)

        provide {
            SettingsImpl(
                    BinaryPreferencesBuilder(androidApplication)
                            .name("AppSettings")
                            .exceptionHandler { Timber.e(it) }
                            .build()
            )
        } bind (Settings::class)

        provide {
            Retrofit.Builder()
                    .baseUrl(BuildConfig.BASE_API_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
                    .create(MovieApi.Service::class.java)
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

        provide { MovieRepositoryImpl(get(REPOSITORY_CONTEXT), get(), get()) } bind (MovieRepository::class)
    }
}