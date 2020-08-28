package info.dvkr.switchmovie.di

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.switchmovie.data.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.repository.local.LocalDateConverter
import info.dvkr.switchmovie.data.repository.local.MovieDao
import info.dvkr.switchmovie.data.repository.local.MovieDb
import info.dvkr.switchmovie.data.repository.local.MovieLocalService
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.viewmodel.moviegrid.MovieGridViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

@Database(entities = [MovieDb::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
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

    factory(KoinQualifier.COMPUTATION_COROUTINE_SCOPE) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    factory(KoinQualifier.IO_COROUTINE_SCOPE) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }


    single { MovieLocalService(get<AppDatabase>().movieDao(), get()) }

    single<MovieRepository.RW> { MovieRepositoryImpl(get(), get()) } bind MovieRepository.RO::class

    single { MoviesUseCase(get(KoinQualifier.COMPUTATION_COROUTINE_SCOPE), get()) }


    viewModel { MovieGridViewModel(get()) }
    viewModel { MovieDetailViewModel(get()) }
}