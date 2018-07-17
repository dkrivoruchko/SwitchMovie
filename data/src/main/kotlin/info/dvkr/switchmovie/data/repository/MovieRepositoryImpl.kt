package info.dvkr.switchmovie.data.repository

import android.arch.lifecycle.LiveData
import info.dvkr.switchmovie.data.repository.api.MovieApiService
import info.dvkr.switchmovie.data.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.usecase.base.Result

class MovieRepositoryImpl(
    private val movieApiService: MovieApiService,
    private val movieLocalService: MovieLocalService
) : MovieRepository.RW {

    override fun clearLoadState() = movieApiService.clearLoadState()

    override suspend fun loadMoreMovies(): Result<List<Movie>> = movieApiService.loadMoreMovies()

    override fun getMovies(): LiveData<List<Movie>> = movieLocalService.getMovies()

    override fun getMovieById(movieId: Int): Movie? = movieLocalService.getMovieById(movieId)

    override fun getMovieByIdLiveData(movieId: Int): LiveData<Movie> = movieLocalService.getMovieByIdLiveData(movieId)

    override fun addMovies(inMovieList: List<Movie>) = movieLocalService.addMovies(inMovieList)

    override fun updateMovie(movie: Movie) = movieLocalService.updateMovie(movie)

    override fun deleteMovies() = movieLocalService.deleteAll()
}