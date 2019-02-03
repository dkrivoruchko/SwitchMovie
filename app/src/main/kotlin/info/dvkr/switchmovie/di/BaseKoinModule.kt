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
import info.dvkr.switchmovie.data.viewmodel.BaseViewModel
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.usecase.base.BaseUseCase
import kotlinx.coroutines.newSingleThreadContext
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

@Database(entities = [MovieLocal.MovieDb::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieLocal.MovieDao
}

val baseKoinModule = module {

    single {
        SettingsImpl(
            BinaryPreferencesBuilder(androidContext())
                .name("AppSettings")
                .exceptionHandler { ex -> XLog.e(ex) }
                .build()
        ) as Settings
    }

    single {
        Room.databaseBuilder(androidContext().applicationContext, AppDatabase::class.java, "SwitchMovieDB").build()
    }


    single("ViewModelCoroutineDispatcher") { newSingleThreadContext("ViewModelContext") }
    factory("ViewModelScope") { BaseViewModel.viewModelScope(get("ViewModelCoroutineDispatcher")) }
    factory("UseCaseScope") { BaseUseCase.useCaseScope }

    single { MovieLocalService(get<AppDatabase>().movieDao(), get()) }
    single { MovieRepositoryImpl(get(), get()) as MovieRepository.RW } bind MovieRepository.RO::class

    single { MoviesUseCase(get("UseCaseScope"), get()) }

    viewModel { MovieGridViewModel(get("ViewModelScope"), get()) }
    viewModel { MovieDetailViewModel(get("ViewModelScope"), get()) }
}