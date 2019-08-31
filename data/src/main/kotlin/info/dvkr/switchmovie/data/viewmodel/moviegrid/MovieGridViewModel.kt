package info.dvkr.switchmovie.data.viewmodel.moviegrid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.data.viewmodel.BaseViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates.observable

class MovieGridViewModel(
        viewModelScope: CoroutineScope,
        private val moviesUseCase: MoviesUseCase
) : BaseViewModel(viewModelScope) {

    data class MovieGridState(
            val moviesLiveData: LiveData<List<Movie>> = MediatorLiveData(),
            val invertMovieStarById: Int = 0,

            val workInProgressCounter: Int = 0,
            val error: Throwable? = null
    ) : State


    private sealed class MovieGridViewModelEvent : Event {
        object Init : MovieGridViewModelEvent()

        override fun toString(): String = this::class.java.simpleName
    }

    override suspend fun onEach(event: Event) {
        XLog.d(getLog("onEach", "$event"))
        when (event) {
            MovieGridViewModelEvent.Init ->
                MoviesUseCase.Request.GetMoviesLiveData()
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { state.updateState { copy(moviesLiveData = it) } }
                        .onFailure { onEvent(Error(it)) }

            MovieGridViewEvent.Refresh ->
                MoviesUseCase.Request.ClearMovies()
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { onEvent(MovieGridViewEvent.LoadMore) }
                        .onFailure { onEvent(Error(it)) }

            MovieGridViewEvent.LoadMore ->
                MoviesUseCase.Request.LoadMoreMovies()
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { }
                        .onFailure { onEvent(Error(it)) }

            MovieGridViewEvent.Update ->
                MoviesUseCase.Request.UpdateMovies()
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { onEvent(MovieGridViewEvent.LoadMore) }
                        .onFailure { onEvent(Error(it)) }

            is MovieGridViewEvent.SetMovieStar ->
                MoviesUseCase.Request.SetMovieStar(event.movieId)
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { }
                        .onFailure { }

            is MovieGridViewEvent.UnsetMovieStar ->
                MoviesUseCase.Request.UnsetMovieStar(event.movieId)
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { }
                        .onFailure { }

            is Error ->
                state.updateState { copy(error = event.throwable) }

            else -> throw IllegalStateException("MovieGridViewModel: Unknown event: $event")
        }
    }

    override fun onError(throwable: Throwable) {
        XLog.e(getLog("onError"), throwable)
        state.updateState { copy(error = throwable) }
    }

    init {
        onEvent(MovieGridViewModelEvent.Init)
    }

    private var state: MovieGridState by observable(MovieGridState()) { _, _, newValue ->
        require(newValue.workInProgressCounter >= 0)
        XLog.d(getLog("State", "$newValue"))
        stateLiveData.postValue(newValue)
    }

    private fun MovieGridState.updateState(block: MovieGridState.() -> MovieGridState) {
        state = block(this)
    }

    private fun MovieGridState.increaseWorkInProgress() = updateState {
        copy(workInProgressCounter = workInProgressCounter + 1)
    }

    private fun MovieGridState.decreaseWorkInProgress() = updateState {
        copy(workInProgressCounter = workInProgressCounter - 1)
    }
}