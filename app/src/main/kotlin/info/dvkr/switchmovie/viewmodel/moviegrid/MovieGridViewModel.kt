package info.dvkr.switchmovie.viewmodel.moviegrid

import androidx.lifecycle.viewModelScope
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlin.properties.Delegates.observable

class MovieGridViewModel(
    private val moviesUseCase: MoviesUseCase
) : BaseViewModel<MovieGridViewModel.MovieGridState>() {

    data class MovieGridState(
        val movies: List<Movie> = emptyList(),
        val invertMovieStarById: Int = 0,

        val workInProgressCounter: Int = 0,
        val error: Throwable? = null
    )

    private sealed class MovieGridViewModelEvent : Event {
        object Init : MovieGridViewModelEvent()

        override fun toString(): String = this.javaClass.simpleName
    }

    override suspend fun onEach(event: Event) {
        XLog.d(getLog("onEach", "$event"))

        when (event) {
            MovieGridViewModelEvent.Init -> {
                MoviesUseCase.Request.GetMoviesFlow().process(moviesUseCase)
                    .onSuccess {
                        it.onEach { movies -> state.update { state.copy(movies = movies) } }
                            .launchIn(viewModelScope + exceptionHandler) // TODO UNSAFE
                    }
                    .onFailure { onError(it) }
            }

            MovieGridViewEvent.Refresh -> doWork {
                MoviesUseCase.Request.ClearMovies().process(moviesUseCase)
                onEvent(MovieGridViewEvent.LoadMore)
            }

            MovieGridViewEvent.LoadMore -> doWork {
                MoviesUseCase.Request.LoadMoreMoviesAsync().process(moviesUseCase)
                    .onFailure { onError(it) }
            }

            MovieGridViewEvent.Update -> doWork {
                MoviesUseCase.Request.ClearOldMovies().process(moviesUseCase)
                onEvent(MovieGridViewEvent.LoadMore)
            }


            is MovieGridViewEvent.SetMovieStar -> doWork {
                MoviesUseCase.Request.SetMovieStarAsync(event.movieId).process(moviesUseCase)
                    .onFailure { } //TODO
            }

            is MovieGridViewEvent.UnsetMovieStar -> doWork {
                MoviesUseCase.Request.ClearMovieStarAsync(event.movieId).process(moviesUseCase)
                    .onFailure { } //TODO
            }

            else -> throw IllegalStateException("MovieGridViewModel: Unknown event: $event")
        }
    }

    override fun onError(throwable: Throwable) {
        state = state.copy(error = throwable)
    }

    init {
        onEvent(MovieGridViewModelEvent.Init)
    }

    private var state: MovieGridState by observable(MovieGridState()) { _, _, newValue ->
        require(newValue.workInProgressCounter >= 0)
        XLog.d(getLog("State", "$newValue"))
        stateLiveData.postValue(newValue)
    }

    private inline fun MovieGridState.update(crossinline block: MovieGridState.() -> MovieGridState) {
        state = block(this)
    }

    private suspend inline fun doWork(crossinline block: suspend () -> Any) =
        try {
            state = state.copy(workInProgressCounter = state.workInProgressCounter + 1)
            block.invoke()
        } finally {
            state = state.copy(workInProgressCounter = state.workInProgressCounter - 1)
        }
}