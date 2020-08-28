package info.dvkr.switchmovie.viewmodel.moviedetail

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

class MovieDetailViewModel(
    private val moviesUseCase: MoviesUseCase
) : BaseViewModel<MovieDetailViewModel.MovieDetailSate>() {

    data class MovieDetailSate(
        val movie: Movie = Movie.EMPTY,

        val workInProgressCounter: Int = 0,
        val error: Throwable? = null
    )

    override suspend fun onEach(event: Event) {
        XLog.d(getLog("onEach", "$event"))

        when (event) {
            is MovieDetailViewEvent.GetMovieById -> doWork {
                MoviesUseCase.Request.GetMovieFlowById(event.movieId)
                    .process(moviesUseCase)
                    .onSuccess {
                        it.onEach { state.update { copy(movie = it) } }
                            .launchIn(viewModelScope + exceptionHandler) // TODO UNSAFE
                    }
                    .onFailure { onError(it) }
            }

            else -> throw IllegalStateException("MovieDetailViewModel: Unknown event: $event")
        }
    }

    override fun onError(throwable: Throwable) {
        state = state.copy(error = throwable)
    }

    private var state: MovieDetailSate by observable(MovieDetailSate()) { _, _, newValue ->
        require(newValue.workInProgressCounter >= 0)
        XLog.d(getLog("State", "$newValue"))
        stateLiveData.postValue(newValue)
    }

    private inline fun MovieDetailSate.update(crossinline block: MovieDetailSate.() -> MovieDetailSate) {
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