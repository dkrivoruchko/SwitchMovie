package info.dvkr.switchmovie.domain.usecase

import android.annotation.SuppressLint
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.domain.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

@SuppressLint("NewApi")
class MoviesUseCase(
    useCaseScope: CoroutineScope,
    private val movieRepository: MovieRepository.RW
) : BaseUseCase(useCaseScope) {

    sealed class Request<R> : BaseUseCase.Request<R>() {
        class GetMoviesFlow : Request<Flow<List<Movie>>>()
        data class GetMovieFlowById(val movieId: Long) : Request<Flow<Movie>>()
        class ClearMovies : Request<Unit>()
        class LoadMoreMoviesAsync : Request<Unit>()
        class ClearOldMovies : Request<Unit>()
        data class SetMovieStarAsync(val movieId: Long) : Request<Unit>()
        data class ClearMovieStarAsync(val movieId: Long) : Request<Unit>()

        override fun toString(): String = this.javaClass.simpleName
    }

    override suspend fun onRequest(request: BaseUseCase.Request<*>) {
        XLog.d(getLog("onRequest", "$request"))

        when (request) {
            is Request.GetMoviesFlow -> request.sendResponse(movieRepository.getMovies())

            is Request.GetMovieFlowById -> request.sendResponse(movieRepository.getMovieFlowById(request.movieId))

            is Request.ClearMovies -> {
                movieRepository.deleteMovies()
                movieRepository.clearLoadState()
                request.sendResponse(Unit)
            }

            is Request.LoadMoreMoviesAsync -> {
                useCaseScope.async {
                    // Exceptions are exposed when calling await, they will be
                    // propagated in the coroutine that called doWork. Watch
                    // out! They will be ignored if the calling context cancels.
                    //  throw IllegalStateException("loadMoreMovies error")
                    movieRepository.loadMoreMovies().map { movieRepository.addMovies(it) }
                } //TODO
                request.sendResponse(Unit)
            }

            is Request.ClearOldMovies -> {
                // if this can throw an exception, wrap inside try/catch
                // or rely on a CoroutineExceptionHandler installed
                // in the externalScope's CoroutineScope
                //   throw IllegalStateException("updateMovies error")
                val lastMovieUpdateDate = movieRepository.getLastMovieUpdateDate()
                if (lastMovieUpdateDate.plusDays(1).isBefore(LocalDate.now())) {
                    movieRepository.deleteMovies()
                    movieRepository.clearLoadState()
                    movieRepository.setLastMovieUpdateDate(LocalDate.now())
                }
                request.sendResponse(Unit)
            }

            is Request.SetMovieStarAsync -> { //TODO
                useCaseScope.launch {
                    delay(10000)
                    val movie = movieRepository.getMovieById(request.movieId)
                    require(movie != null) { "MoviesUseCase.setMovieStar: Movie (id:${request.movieId}) not found" }
                    movieRepository.updateMovie(movie.copy(isStar = true))
                }
                request.sendResponse(Unit)
            }

            is Request.ClearMovieStarAsync -> { //TODO
                useCaseScope.launch {
                    val movie = movieRepository.getMovieById(request.movieId)
                    require(movie != null) { "MoviesUseCase.unsetMovieStar: Movie (id:${request.movieId}) not found" }
                    movieRepository.updateMovie(movie.copy(isStar = false))
                }
                request.sendResponse(Unit)
            }

            else -> throw IllegalArgumentException("MoviesUseCase: Unknown request: $request")
        }
    }
}