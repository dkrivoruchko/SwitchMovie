package info.dvkr.switchmovie.data.viewmodel.moviedetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.spotify.mobius.*
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.runners.WorkRunner
import info.dvkr.switchmovie.data.viewmodel.BaseViewStateViewModel
import info.dvkr.switchmovie.data.viewmodel.MobiusLogger
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getTag
import timber.log.Timber

class MovieDetailViewModel(
    private val eventWorkRunner: WorkRunner,
    private val effectWorkRunner: WorkRunner,
    private val moviesUseCase: MoviesUseCase
) : BaseViewStateViewModel<MovieDetailModel, MovieDetailEvent, MovieDetailEffect>() {

    private val movieDetailModelLiveData = MutableLiveData<MovieDetailModel>()
    fun getMovieDetailModelLiveData(): LiveData<MovieDetailModel> = movieDetailModelLiveData

    private val update = Update<MovieDetailModel, MovieDetailEvent, MovieDetailEffect> { model, event ->
        return@Update when (event) {
            is MovieDetailEvent.ViewGetMovieById -> Next.dispatch(setOf(MovieDetailEffect.GetMovieById(event.movieId)))

            is MovieDetailEvent.OnMovie -> Next.next(model.copy(movieLiveData = event.movieLiveData))

            MovieDetailEvent.WorkStart -> Next.next(model.copy(workInProgressCounter = model.workInProgressCounter + 1))

            MovieDetailEvent.WorkFinish -> {
                val workInProgress = model.workInProgressCounter - 1
                workInProgress >= 0 || throw IllegalStateException("workInProgressCounter: $workInProgress < 0")
                Next.next(model.copy(workInProgressCounter = workInProgress))
            }

            is MovieDetailEvent.Error -> Next.next(model.copy(error = event.error))

            else -> throw IllegalStateException("Unknown MovieDetailEvent: $event")
        }
    }

    private val effectHandler = Connectable<MovieDetailEffect, MovieDetailEvent> { eventConsumer ->
        object : Connection<MovieDetailEffect> {
            override fun accept(effect: MovieDetailEffect) = when (effect) {
                is MovieDetailEffect.GetMovieById -> runEffect {
                    eventConsumer.accept(MovieDetailEvent.WorkStart)
                    effectGetMovieById(effect.movieId, eventConsumer)
                    eventConsumer.accept(MovieDetailEvent.WorkFinish)
                }
            }

            override fun dispose() = Unit  // We don't have any resources to release
        }
    }

    private suspend fun effectGetMovieById(movieId: Int, eventConsumer: Consumer<MovieDetailEvent>) =
        MoviesUseCase.Request.GetMovieByIdLiveData(movieId).process(moviesUseCase).onResult { resultEither ->
            Timber.tag(getTag("effectGetMovieById")).d(resultEither.toString())

            resultEither.either(
                { sendEventInternal(MovieDetailEvent.Error(it)) },
                { eventConsumer.accept(MovieDetailEvent.OnMovie(it)) }
            )
        }

    override val loop: MobiusLoop<MovieDetailModel, MovieDetailEvent, MovieDetailEffect> =
        Mobius.loop(update, effectHandler)
            .eventSource(eventSource)
            .eventRunner { eventWorkRunner }
            .effectRunner { effectWorkRunner }
            .logger(MobiusLogger(this.javaClass.simpleName, ::onException))
            .startFrom(MovieDetailModel())
            .apply {
                observe { movieDetailModelLiveData.postValue(it) }
            }

    override fun onException(exception: Throwable) {
        super.onException(exception)
        sendEventInternal(MovieDetailEvent.Error(exception))
    }
}