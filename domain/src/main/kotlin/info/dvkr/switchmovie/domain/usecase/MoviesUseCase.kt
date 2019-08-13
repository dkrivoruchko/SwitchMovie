package info.dvkr.switchmovie.domain.usecase

import androidx.lifecycle.LiveData
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.usecase.base.BaseUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.domain.utils.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import org.threeten.bp.LocalDate

class MoviesUseCase(
    useCaseScope: CoroutineScope,
    private val movieRepository: MovieRepository.RW
) : BaseUseCase(useCaseScope) {

    sealed class Request<R> : BaseUseCase.Request<R>() {
        class GetMoviesLiveData : Request<LiveData<List<Movie>>>()
        data class GetMovieByIdLiveData(val movieId: Int) : Request<LiveData<Movie>>()
        class ClearMovies : Request<Unit>()
        class UpdateMovies : Request<Unit>()
        class LoadMoreMovies : Request<Unit>()
        data class SetMovieStar(val movieId: Int) : Request<Unit>()
        data class UnsetMovieStar(val movieId: Int) : Request<Unit>()

        override fun toString(): String = this::class.java.simpleName
    }

    override suspend fun requestHandler(request: BaseUseCase.Request<*>) {
        XLog.d(getLog("requestHandler", "$request"))

        when (request) {
            is Request.GetMoviesLiveData -> getMoviesLiveData(request)
            is Request.GetMovieByIdLiveData -> getMovieByIdLiveData(request)
            is Request.ClearMovies -> clearMovies(request)
            is Request.UpdateMovies -> updateMovies(request)
            is Request.LoadMoreMovies -> loadMoreMoviesAsync(request)
            is Request.SetMovieStar -> setMovieStar(request)
            is Request.UnsetMovieStar -> unsetMovieStar(request)
        }
    }

    private suspend fun getMoviesLiveData(request: Request.GetMoviesLiveData) = request.sendResponse {
        movieRepository.getMovies()
    }

    private suspend fun getMovieByIdLiveData(request: Request.GetMovieByIdLiveData) = request.sendResponse {
        movieRepository.getMovieByIdLiveData(request.movieId)
    }

    private suspend fun clearMovies(request: Request.ClearMovies) = request.sendResponse {
        movieRepository.deleteMovies()
        movieRepository.clearLoadState()
    }

    private suspend fun updateMovies(request: Request.UpdateMovies) = request.sendResponse {
        val lastMovieUpdateDate = movieRepository.getLastMovieUpdateDate()
        if (lastMovieUpdateDate.plusDays(1).isBefore(LocalDate.now())) {
            movieRepository.deleteMovies()
            movieRepository.clearLoadState()
            movieRepository.setLastMovieUpdateDate(LocalDate.now())
        } else {
            throw IllegalStateException("Movies already updated")
        }
    }

    private suspend fun loadMoreMoviesAsync(request: Request.LoadMoreMovies) = async {
        request.sendResponse {
            movieRepository.loadMoreMovies()
                .map { movieRepository.addMovies(it) }
        }
    }

    private suspend fun setMovieStar(request: Request.SetMovieStar) = request.sendResponse {
        val movie = movieRepository.getMovieById(request.movieId)
        require(movie != null) { "MoviesUseCase.setMovieStar: Movie (id:${request.movieId}) not found" }
        movieRepository.updateMovie(movie.copy(isStar = true))
    }

    private suspend fun unsetMovieStar(request: Request.UnsetMovieStar) = request.sendResponse {
        val movie = movieRepository.getMovieById(request.movieId)
        require(movie != null) { "MoviesUseCase.unsetMovieStar: Movie (id:${request.movieId}) not found" }
        movieRepository.updateMovie(movie.copy(isStar = false))
    }
}