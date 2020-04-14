package info.dvkr.switchmovie.viewmodel.moviegrid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import info.dvkr.switchmovie.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlin.properties.Delegates.observable

class MovieGridViewModel(
    viewModelScope: CoroutineScope,
    private val moviesUseCase: MoviesUseCase
) : BaseViewModel(viewModelScope) {

    data class MovieGridState(
        val movies: List<Movie> = emptyList(),
        val invertMovieStarById: Int = 0,

        val workInProgressCounter: Int = 0,
        val error: Throwable? = null
    )

    private sealed class MovieGridViewModelEvent : Event {
        object Init : MovieGridViewModelEvent()

        override fun toString(): String = this::class.java.simpleName
    }

    override suspend fun onEach(event: Event) {
        XLog.d(getLog("onEach", "$event"))

        when (event) {
            MovieGridViewModelEvent.Init -> {
                moviesUseCase.getMoviesFlow()
                    .onEach { movies -> state.update { state.copy(movies = movies) } }
                    .launchIn(viewModelScope + exceptionHandler)
            }

            MovieGridViewEvent.Refresh -> doWork {
                moviesUseCase.clearMovies()
                onEvent(MovieGridViewEvent.LoadMore)
            }

            MovieGridViewEvent.LoadMore -> doWork {
                moviesUseCase.loadMoreMovies()
                    .onFailure { state.update { copy(error = it) } }
            }

            MovieGridViewEvent.Update -> doWork {
                moviesUseCase.clearOldMovies() // BAD Name
                onEvent(MovieGridViewEvent.LoadMore)
            }


            is MovieGridViewEvent.SetMovieStar -> doWork {
                moviesUseCase.setMovieStar(event.movieId)
            }

            is MovieGridViewEvent.UnsetMovieStar -> doWork {
                moviesUseCase.unsetMovieStar(event.movieId)
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

    fun stateLiveData(): LiveData<MovieGridState> = Transformations.distinctUntilChanged(_stateLiveData)

    private val _stateLiveData = MutableLiveData<MovieGridState>()

    private var state: MovieGridState by observable(MovieGridState()) { _, _, newValue ->
        require(newValue.workInProgressCounter >= 0)
        XLog.d(getLog("State", "$newValue"))
        _stateLiveData.postValue(newValue)
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