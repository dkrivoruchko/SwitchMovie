package info.dvkr.switchmovie.data.dagger.module

import dagger.Module
import dagger.Provides
import info.dvkr.switchmovie.data.movie.repository.MovieRepositoryImpl
import info.dvkr.switchmovie.data.movie.repository.api.ApiRepository
import info.dvkr.switchmovie.data.movie.repository.local.LocalRepository
import info.dvkr.switchmovie.domain.repository.MovieRepository
import javax.inject.Singleton

@Singleton
@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideMovieRepository(apiRepository: ApiRepository,
                               localRepository: LocalRepository): MovieRepository =
            MovieRepositoryImpl(apiRepository, localRepository)
}