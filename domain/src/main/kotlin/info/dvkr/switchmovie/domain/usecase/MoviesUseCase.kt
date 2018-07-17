package info.dvkr.switchmovie.domain.usecase

import android.arch.lifecycle.LiveData
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.usecase.base.BaseUseCase
import info.dvkr.switchmovie.domain.usecase.base.Result
import info.dvkr.switchmovie.domain.usecase.base.UseCaseRequest
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import timber.log.Timber

class MoviesUseCase(
    private val movieRepository: MovieRepository.RW
) : BaseUseCase() {

    sealed class MoviesUseCaseRequest<R> : UseCaseRequest<R>() {
        class GetMoviesLiveData : MoviesUseCaseRequest<LiveData<List<Movie>>>()
        data class GetMovieByIdLiveData(val movieId: Int) : MoviesUseCaseRequest<LiveData<Movie>>()
        class ClearMovies : MoviesUseCaseRequest<Unit>()
        class LoadMoreMovies : MoviesUseCaseRequest<Unit>()
        data class InvertMovieStarById(val id: Int) : MoviesUseCaseRequest<Unit>()
    }

    // TODO Parent ?
    override val sendChannel: SendChannel<UseCaseRequest<*>> = actor(coroutineContext, Channel.UNLIMITED) {
        for (request in this) try {
            Timber.d("MoviesUseCase: [${Utils.getLogPrefix(this)}] request: $request")
            when (request) {
                is MoviesUseCaseRequest.GetMoviesLiveData -> getMoviesLiveData(request)
                is MoviesUseCaseRequest.GetMovieByIdLiveData -> getMovieByIdLiveData(request)
                is MoviesUseCaseRequest.ClearMovies -> clearMovies(request)
                is MoviesUseCaseRequest.LoadMoreMovies -> loadMoreMovies(request)
                is MoviesUseCaseRequest.InvertMovieStarById -> invertMovieStarById(request)
            }
        } catch (ex: Exception) {
            request.sendResult(Result.Error(ex, "MoviesUseCase:"))
        }
    }

    private fun getMoviesLiveData(moviesUseCaseRequest: MoviesUseCaseRequest.GetMoviesLiveData) {
        moviesUseCaseRequest.sendResult(Result.Success(movieRepository.getMovies()))
    }

    private fun getMovieByIdLiveData(moviesUseCaseRequest: MoviesUseCaseRequest.GetMovieByIdLiveData) {
        moviesUseCaseRequest.sendResult(Result.Success(movieRepository.getMovieByIdLiveData(moviesUseCaseRequest.movieId)))
    }

    private fun clearMovies(moviesUseCaseRequest: MoviesUseCaseRequest.ClearMovies) {
        movieRepository.deleteMovies()
        movieRepository.clearLoadState()
        moviesUseCaseRequest.sendResult(Result.Success(Unit))
    }

    private suspend fun loadMoreMovies(moviesUseCaseRequest: MoviesUseCaseRequest.LoadMoreMovies) {
        val result = movieRepository.loadMoreMovies()

        when (result) {
            is Result.Success -> {
                movieRepository.addMovies(result.data)
                moviesUseCaseRequest.sendResult(Result.Success(Unit))
            }
            is Result.Error -> moviesUseCaseRequest.sendResult(
                Result.Error(result.exception, "MoviesUseCase.loadMoreMovies:")
            )
            is Result.InProgress -> moviesUseCaseRequest.sendResult(Result.InProgress) //TODO
        }
    }


    private fun invertMovieStarById(moviesUseCaseRequest: MoviesUseCaseRequest.InvertMovieStarById) {
        val movieToStar = movieRepository.getMovieById(moviesUseCaseRequest.id)

        val result = movieToStar?.let {
            movieRepository.updateMovie(it.copy(isStar = it.isStar.not()))
            Result.Success(Unit)
        } ?: Result.Error(NoSuchElementException(), "MoviesUseCase.invertMovieStarById:")

        moviesUseCaseRequest.sendResult(result)
    }
}