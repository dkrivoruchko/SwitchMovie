package info.dvkr.switchmovie.di

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import com.spotify.mobius.runners.WorkRunners
import info.dvkr.switchmovie.data.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.repository.local.MovieLocal
import info.dvkr.switchmovie.data.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getTag
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import timber.log.Timber

@Database(entities = [MovieLocal.MovieDb::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieLocal.MovieDao
}

const val MOBIUS_EVENT_WORK_RUNNER = "EventWorkRunner"
const val MOBIUS_EFFECT_WORK_RUNNER = "EffectWorkRunner"

val baseKoinModule = module {

    single {
        SettingsImpl(
            BinaryPreferencesBuilder(androidContext())
                .name("AppSettings")
                .exceptionHandler { ex -> Timber.tag(getTag("AppSettings")).e(ex) }
                .build()
        ) as Settings
    }

    single {
        Room.databaseBuilder(androidContext().applicationContext, AppDatabase::class.java, "SwitchMovieDB").build()
    }

    single {
        BinaryPreferencesBuilder(androidContext())
            .name("MoviePreferences")
            .exceptionHandler { ex -> Timber.tag(getTag("MoviePreferences")).e(ex) }
            .build() as Preferences
    }

    single { MovieLocalService(get<AppDatabase>().movieDao(), get()) }

    single { MovieRepositoryImpl(get(), get()) as MovieRepository.RW } bind MovieRepository.RO::class

    single { MoviesUseCase(get()) }

    single(MOBIUS_EVENT_WORK_RUNNER) {
        WorkRunners.singleThread().apply { post { Thread.currentThread().name = MOBIUS_EVENT_WORK_RUNNER } }
    }
    single(MOBIUS_EFFECT_WORK_RUNNER) {
        WorkRunners.singleThread().apply { post { Thread.currentThread().name = MOBIUS_EFFECT_WORK_RUNNER } }
    }

    viewModel { MovieGridViewModel(get(MOBIUS_EVENT_WORK_RUNNER), get(MOBIUS_EFFECT_WORK_RUNNER), get()) }
    viewModel { MovieDetailViewModel(get(MOBIUS_EVENT_WORK_RUNNER), get(MOBIUS_EFFECT_WORK_RUNNER), get()) }

}