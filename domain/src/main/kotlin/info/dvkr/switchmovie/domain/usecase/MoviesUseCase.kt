package info.dvkr.switchmovie.domain.usecase

import android.annotation.SuppressLint
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.map
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@SuppressLint("NewApi")
class MoviesUseCase(
    private val useCaseScope: CoroutineScope,
    private val movieRepository: MovieRepository.RW
) {

    suspend fun getMoviesFlow(): Flow<List<Movie>> = withContext(useCaseScope.coroutineContext) {
//        throw IllegalStateException("getMoviesFlow error")
        movieRepository.getMovies()
    }

    suspend fun getMovieFlowById(movieId: Long): Flow<Movie> = withContext(useCaseScope.coroutineContext) {
//        throw IllegalStateException("getMovieFlowById error")
        movieRepository.getMovieFlowById(movieId)
    }

    suspend fun clearMovies() = useCaseScope.launch {
        movieRepository.deleteMovies()
        movieRepository.clearLoadState()
    }.join()

    suspend fun loadMoreMovies(): Either<Throwable, Unit> = useCaseScope.async {
        // Exceptions are exposed when calling await, they will be
        // propagated in the coroutine that called doWork. Watch
        // out! They will be ignored if the calling context cancels.
//        throw IllegalStateException("loadMoreMovies error")
        movieRepository.loadMoreMovies().map { movieRepository.addMovies(it) }
    }.await()

    suspend fun clearOldMovies() = useCaseScope.launch {
        // if this can throw an exception, wrap inside try/catch
        // or rely on a CoroutineExceptionHandler installed
        // in the externalScope's CoroutineScope
//        throw IllegalStateException("updateMovies error")
        val lastMovieUpdateDate = movieRepository.getLastMovieUpdateDate()
        if (lastMovieUpdateDate.plusDays(1).isBefore(LocalDate.now())) {
            movieRepository.deleteMovies()
            movieRepository.clearLoadState()
            movieRepository.setLastMovieUpdateDate(LocalDate.now())
        }
    }.join()

    suspend fun setMovieStar(movieId: Long) = useCaseScope.launch {
        val movie = movieRepository.getMovieById(movieId)
        delay(10000)
        require(movie != null) { "MoviesUseCase.setMovieStar: Movie (id:${movieId}) not found" }
        movieRepository.updateMovie(movie.copy(isStar = true))
    }.join()

    suspend fun unsetMovieStar(movieId: Long) = useCaseScope.launch {
        val movie = movieRepository.getMovieById(movieId)
        require(movie != null) { "MoviesUseCase.unsetMovieStar: Movie (id:${movieId}) not found" }
        movieRepository.updateMovie(movie.copy(isStar = false))
    }.join()
}