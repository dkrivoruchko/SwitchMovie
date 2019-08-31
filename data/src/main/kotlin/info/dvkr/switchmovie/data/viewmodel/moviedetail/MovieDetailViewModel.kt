package info.dvkr.switchmovie.data.viewmodel.moviedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.data.viewmodel.BaseViewModel
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.Delegates.observable

class MovieDetailViewModel(
        viewModelScope: CoroutineScope,
        private val moviesUseCase: MoviesUseCase
) : BaseViewModel(viewModelScope) {

    data class MovieDetailSate(
            val movieLiveData: LiveData<Movie> = MediatorLiveData(),

            val workInProgressCounter: Int = 0,
            val error: Throwable? = null
    ) : State

    override suspend fun onEach(event: Event) {
        XLog.d(getLog("onEach", "$event"))
        when (event) {
            is MovieDetailViewEvent.GetMovieById ->
                MoviesUseCase.Request.GetMovieByIdLiveData(event.movieId)
                        .onStart { state.increaseWorkInProgress() }
                        .process(moviesUseCase)
                        .onAny { state.decreaseWorkInProgress() }
                        .onSuccess { state.updateState { copy(movieLiveData = it) } }
                        .onFailure { state.updateState { copy(error = it) } }

            else -> throw IllegalStateException("MovieDetailViewModel: Unknown event: $event")
        }
    }

    override fun onError(throwable: Throwable) {
        XLog.e(getLog("onError"), throwable)
        state.updateState { copy(error = throwable) }
    }

    private var state: MovieDetailSate by observable(MovieDetailSate()) { _, _, newValue ->
        require(newValue.workInProgressCounter >= 0)
        XLog.d(getLog("Sate", "$newValue"))
        stateLiveData.postValue(newValue)
    }

    private fun MovieDetailSate.updateState(block: MovieDetailSate.() -> MovieDetailSate) {
        state = block(this)
    }

    private fun MovieDetailSate.increaseWorkInProgress() = updateState {
        copy(workInProgressCounter = workInProgressCounter + 1)
    }

    private fun MovieDetailSate.decreaseWorkInProgress() = updateState {
        copy(workInProgressCounter = workInProgressCounter - 1)
    }
}