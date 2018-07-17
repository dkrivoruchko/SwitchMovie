package info.dvkr.switchmovie.di

import com.ironz.binaryprefs.BinaryPreferencesBuilder
import info.dvkr.switchmovie.data.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.settings.SettingsImpl
import info.dvkr.switchmovie.data.viewmodel.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.data.viewmodel.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import timber.log.Timber


val baseKoinModule = module {

    single {
        SettingsImpl(
            BinaryPreferencesBuilder(androidContext())
                .name("AppSettings")
                .exceptionHandler { Timber.e(it) }
                .build()
        ) as Settings
    }

    single { MovieRepositoryImpl(get(), get()) as MovieRepository.RW } bind MovieRepository.RO::class

    single { MoviesUseCase(get()) }

    viewModel { MovieGridViewModel(get()) }

    viewModel { MovieDetailViewModel(get()) }
}