package info.dvkr.switchmovie.data.repository

import android.annotation.SuppressLint
import info.dvkr.switchmovie.data.repository.api.MovieApiService
import info.dvkr.switchmovie.data.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.utils.Either
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@SuppressLint("NewApi")
class MovieRepositoryImpl(
    private val movieApiService: MovieApiService,
    private val movieLocalService: MovieLocalService
) : MovieRepository.RW {

    // RO

    override fun getMovies(): Flow<List<Movie>> = movieLocalService.getMovies()

    override suspend fun getMovieById(movieId: Long): Movie? =
        movieLocalService.getMovieById(movieId)

    override fun getMovieFlowById(movieId: Long): Flow<Movie> =
        movieLocalService.getMovieFlowById(movieId)

    override fun getLastMovieUpdateDate(): LocalDate = movieLocalService.getLastMovieUpdateDate()

    // RW

    override fun clearLoadState() = movieApiService.clearLoadState()

    override suspend fun loadMoreMovies(): Either<Throwable, List<Movie>> =
        movieApiService.loadMoreMovies()

    override suspend fun addMovies(inMovieList: List<Movie>) =
        movieLocalService.addMovies(inMovieList)

    override suspend fun updateMovie(movie: Movie) = movieLocalService.updateMovie(movie)

    override suspend fun deleteMovies() = movieLocalService.deleteAll()

    override fun setLastMovieUpdateDate(localDate: LocalDate) =
        movieLocalService.setLastMovieUpdateDate(localDate)
}