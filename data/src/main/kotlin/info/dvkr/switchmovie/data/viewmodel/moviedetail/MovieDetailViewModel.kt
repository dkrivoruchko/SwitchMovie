package info.dvkr.switchmovie.data.viewmodel.moviedetail

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

class MovieDetailViewModel(
    viewModelScope: CoroutineScope,
    private val moviesUseCase: MoviesUseCase
) : BaseViewModel(viewModelScope) {

    data class MovieDetailSate(
        val movieLiveData: LiveData<Movie> = MediatorLiveData(),
        val workInProgressCounter: Int = 0,
        val error: Throwable? = null
    )

    private var movieDetailState = MovieDetailSate()
    private val movieDetailStateLiveData = MutableLiveData<MovieDetailSate>()

    fun getMovieDetailSateLiveData(): LiveData<MovieDetailSate> =
        Transformations.distinctUntilChanged(movieDetailStateLiveData)

    private fun MovieDetailSate.updateState(block: MovieDetailSate.() -> MovieDetailSate) {
        movieDetailState = block(this)
        XLog.d(getLog("updateState", "New state: $movieDetailState"))
        movieDetailStateLiveData.postValue(movieDetailState)
    }

    private fun MovieDetailSate.increaseWorkInProgress() = updateState {
        copy(workInProgressCounter = workInProgressCounter + 1)
    }

    private fun MovieDetailSate.decreaseWorkInProgress() = updateState {
        require(workInProgressCounter > 0) { "workInProgressCounter: $workInProgressCounter <= 0" }
        copy(workInProgressCounter = workInProgressCounter - 1)
    }

    override val viewModelEventChannel: SendChannel<BaseViewModel.Event> = actor(capacity = 8) {
        consumeEach { event ->
            try {
                when (event) {
                    is MovieDetailViewEvent.GetMovieById ->
                        MoviesUseCase.Request.GetMovieByIdLiveData(event.movieId)
                            .onStart { movieDetailState.increaseWorkInProgress() }
                            .process(moviesUseCase)
                            .onAny { movieDetailState.decreaseWorkInProgress() }
                            .onSuccess { movieDetailState.updateState { copy(movieLiveData = it) } }
                            .onFailure { movieDetailState.updateState { copy(error = it) } }

                    else -> throw IllegalStateException("MovieDetailViewModel: Unknown event: $event")
                }
            } catch (throwable: Throwable) {
                XLog.e(this@MovieDetailViewModel.getLog("actor"), throwable)
                movieDetailState.updateState { copy(error = throwable) }
            }
        }
    }
}