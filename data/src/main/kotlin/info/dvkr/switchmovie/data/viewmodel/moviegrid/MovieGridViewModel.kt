package info.dvkr.switchmovie.data.viewmodel.moviegrid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.data.viewmodel.BaseViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach

class MovieGridViewModel(
    viewModelScope: CoroutineScope,
    private val moviesUseCase: MoviesUseCase
) : BaseViewModel(viewModelScope) {

    data class MovieGridState(
        val moviesLiveData: LiveData<List<Movie>> = MediatorLiveData(),
        val invertMovieStarById: Int = 0,
        val workInProgressCounter: Int = 0,
        val error: Throwable? = null
    )

    private var movieGridState = MovieGridState()
    private val movieGridStateLiveData = MutableLiveData<MovieGridState>()

    fun getMovieGridStateLiveData(): LiveData<MovieGridState> =
        Transformations.distinctUntilChanged(movieGridStateLiveData)

    private fun MovieGridState.updateState(block: MovieGridState.() -> MovieGridState) {
        movieGridState = block(this)
        XLog.d(getLog("updateState", "New state: $movieGridState"))
        movieGridStateLiveData.postValue(movieGridState)
    }

    private fun MovieGridState.increaseWorkInProgress() = updateState {
        copy(workInProgressCounter = workInProgressCounter + 1)
    }

    private fun MovieGridState.decreaseWorkInProgress() = updateState {
        require(workInProgressCounter > 0) { "workInProgressCounter: $workInProgressCounter <= 0" }
        copy(workInProgressCounter = workInProgressCounter - 1)
    }

    private sealed class MovieGridViewModelEvent : Event {
        object Init : MovieGridViewModelEvent()

        override fun toString(): String = this::class.java.simpleName
    }

    override val viewModelEventChannel: SendChannel<Event> = actor(capacity = 8) {
        consumeEach { event ->
            try {
                when (event) {
                    MovieGridViewModelEvent.Init ->
                        MoviesUseCase.Request.GetMoviesLiveData()
                            .onStart { movieGridState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieGridState.decreaseWorkInProgress() }
                            .onSuccess { movieGridState.updateState { copy(moviesLiveData = it) } }
                            .onFailure { onEvent(Error(it)) }

                    MovieGridViewEvent.Refresh ->
                        MoviesUseCase.Request.ClearMovies()
                            .onStart { movieGridState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieGridState.decreaseWorkInProgress() }
                            .onSuccess { onEvent(MovieGridViewEvent.LoadMore) }
                            .onFailure { onEvent(Error(it)) }

                    MovieGridViewEvent.LoadMore ->
                        MoviesUseCase.Request.LoadMoreMovies()
                            .onStart { movieGridState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieGridState.decreaseWorkInProgress() }
                            .onSuccess { }
                            .onFailure { onEvent(Error(it)) }

                    MovieGridViewEvent.Update ->
                        MoviesUseCase.Request.UpdateMovies()
                            .onStart { movieGridState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieGridState.decreaseWorkInProgress() }
                            .onSuccess { onEvent(MovieGridViewEvent.LoadMore) }
                            .onFailure { onEvent(Error(it)) }

                    is MovieGridViewEvent.SetMovieStar ->
                        MoviesUseCase.Request.SetMovieStar(event.movieId)
                            .onStart { movieGridState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieGridState.decreaseWorkInProgress() }
                            .onSuccess { }
                            .onFailure { }

                    is MovieGridViewEvent.UnsetMovieStar ->
                        MoviesUseCase.Request.UnsetMovieStar(event.movieId)
                            .onStart { movieGridState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieGridState.decreaseWorkInProgress() }
                            .onSuccess { }
                            .onFailure { }

                    is Error ->
                        movieGridState.updateState { copy(error = event.throwable) }

                    else -> throw IllegalStateException("MovieGridViewModel: Unknown event: $event")
                }
            } catch (throwable: Throwable) {
                XLog.e(this@MovieGridViewModel.getLog("actor"), throwable)
                movieGridState.updateState { copy(error = throwable) }
            }
        }
    }

    init {
        onEvent(MovieGridViewModelEvent.Init)
    }
}