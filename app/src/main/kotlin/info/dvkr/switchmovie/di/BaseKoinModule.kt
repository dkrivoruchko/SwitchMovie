package info.dvkr.switchmovie.di

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.switchmovie.data.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.repository.local.MovieLocal
import info.dvkr.switchmovie.data.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.*
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

@Database(entities = [MovieLocal.MovieDb::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieLocal.MovieDao
}

val baseKoinModule = module {

    single<Settings> {
        SettingsImpl(
            BinaryPreferencesBuilder(androidApplication())
                .name("AppSettings")
                .exceptionHandler { ex -> XLog.e(ex) }
                .build()
        )
    }

    single<AppDatabase> {
        Room.databaseBuilder(androidApplication(), AppDatabase::class.java, "SwitchMovieDB").build()
    }

    single(named("ViewModelCoroutineDispatcher")) { newSingleThreadContext("ViewModelContext") }

    factory { SupervisorJob() }

    factory(named("ViewModelScope")) {
        CoroutineScope(get<CompletableJob>() +
                get<ExecutorCoroutineDispatcher>(named("ViewModelCoroutineDispatcher")) +
                CoroutineExceptionHandler { _, throwable -> XLog.e(getLog("onCoroutineException"), throwable) }
        )
    }

    factory(named("UseCaseScope")) {
        CoroutineScope(get<CompletableJob>() +
                Dispatchers.Default +
                CoroutineExceptionHandler { _, throwable -> XLog.e(getLog("onCoroutineException"), throwable) }
        )
    }

    single { MovieLocalService(get<AppDatabase>().movieDao(), get()) }

    single<MovieRepository.RW> { MovieRepositoryImpl(get(), get()) } bind MovieRepository.RO::class

    single { MoviesUseCase(get(named("UseCaseScope")), get()) }

    viewModel { MovieGridViewModel(get(named("ViewModelScope")), get()) }
    viewModel { MovieDetailViewModel(get(named("ViewModelScope")), get()) }
}