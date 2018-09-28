package info.dvkr.switchmovie.domain.usecase

import android.arch.lifecycle.LiveData
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.usecase.base.BaseUseCase
import info.dvkr.switchmovie.domain.usecase.base.BaseUseCaseRequest
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.getTag
import kotlinx.coroutines.experimental.CoroutineName
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import org.threeten.bp.LocalDate
import timber.log.Timber

class MoviesUseCase(
    private val movieRepository: MovieRepository.RW
) : BaseUseCase() {

    sealed class Request<R> : BaseUseCaseRequest<R>() {
        class GetMoviesLiveData : Request<LiveData<List<Movie>>>()
        data class GetMovieByIdLiveData(val movieId: Int) : Request<LiveData<Movie>>()
        class ClearMovies : Request<Unit>()
        class UpdateMovies : Request<Unit>()
        class LoadMoreMovies : Request<Unit>()
        data class InvertMovieStarById(val id: Int) : Request<Unit>()
    }

    override val sendChannel: SendChannel<BaseUseCaseRequest<*>> =
        actor(coroutineContext + CoroutineName("MoviesUseCaseActor"), capacity = 8) {
            for (request in this) try {
                Timber.tag(this@MoviesUseCase.getTag(request.javaClass.simpleName)).d(request.toString())

                when (request) {
                    is Request.GetMoviesLiveData -> getMoviesLiveData(request)
                    is Request.GetMovieByIdLiveData -> getMovieByIdLiveData(request)
                    is Request.ClearMovies -> clearMovies(request)
                    is Request.UpdateMovies -> updateMovies(request)
                    is Request.LoadMoreMovies -> loadMoreMovies(request)
                    is Request.InvertMovieStarById -> invertMovieStarById(request)
                }
            } catch (ex: Exception) {
                request.sendResult(Either.Left(ex))
            }
        }

    private fun getMoviesLiveData(request: Request.GetMoviesLiveData) {
        request.sendResult(Either.Right(movieRepository.getMovies()))
    }

    private fun getMovieByIdLiveData(request: Request.GetMovieByIdLiveData) {
        request.sendResult(Either.Right(movieRepository.getMovieByIdLiveData(request.movieId)))
    }

    private fun clearMovies(request: Request.ClearMovies) {
        movieRepository.deleteMovies()
        movieRepository.clearLoadState()
        request.sendResult(Either.Right(Unit))
    }

    private fun updateMovies(request: Request.UpdateMovies) {
        val lastMovieUpdateDate = movieRepository.getLastMovieUpdateDate()
        if (lastMovieUpdateDate.plusDays(1).isBefore(LocalDate.now())) {
            movieRepository.deleteMovies()
            movieRepository.clearLoadState()
            movieRepository.setLastMovieUpdateDate(LocalDate.now())
            request.sendResult(Either.Right(Unit))
        } else {
            request.sendResult(Either.Left(IllegalStateException("Movies already updated")))
        }
    }

    private suspend fun loadMoreMovies(request: Request.LoadMoreMovies) {
        movieRepository.loadMoreMovies().either(
            { request.sendResult(Either.Left(it)) },
            {
                movieRepository.addMovies(it)
                request.sendResult(Either.Right(Unit))
            }
        )
    }

    private fun invertMovieStarById(request: Request.InvertMovieStarById) {
        val movie = movieRepository.getMovieById(request.id)

        val result = if (movie != null) {
            movieRepository.updateMovie(movie.copy(isStar = movie.isStar.not()))
            Either.Right(Unit)
        } else {
            Either.Left(NoSuchElementException("MoviesUseCase.invertMovieStarById"))
        }

        request.sendResult(result)
    }
}