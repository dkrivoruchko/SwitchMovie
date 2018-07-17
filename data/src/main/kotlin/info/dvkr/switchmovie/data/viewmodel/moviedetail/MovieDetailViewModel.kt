package info.dvkr.switchmovie.data.viewmodel.moviedetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.MainThread
import com.spotify.mobius.*
import com.spotify.mobius.android.AndroidLogger
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.runners.WorkRunners
import info.dvkr.switchmovie.data.viewmodel.BaseViewStateViewModel
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.usecase.base.Result
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import timber.log.Timber

class MovieDetailViewModel(
    private val moviesUseCase: MoviesUseCase
) : BaseViewStateViewModel() {

    private val movieDetailModelLiveData = MutableLiveData<MovieDetailModel>()
    fun getMovieDetailModelLiveData(): LiveData<MovieDetailModel> = movieDetailModelLiveData

    private val update = Update<MovieDetailModel, MovieDetailEvent, MovieDetailEffect> { model, event ->
        Timber.e("[${Utils.getLogPrefix(this)}] Update: $event")

        return@Update when (event) {
            is MovieDetailEvent.ViewGetMovieById -> Next.dispatch(setOf(MovieDetailEffect.GetMovieById(event.movieId)))

//            is MovieDetailEvent.ViewInvertMovieStarById -> {
//                val invertMovieStarByIdJob = Pair(event.id, Job())
//                Next.next(
//                    model.copy(invertMovieStarByIdJob = invertMovieStarByIdJob),
//                    setOf(MovieDetailEffect.InvertMovieStarById(invertMovieStarByIdJob))
//                )
//            }
//
            is MovieDetailEvent.OnMovie -> Next.next(model.copy(movieLiveData = event.movieLiveData))
//
//            is MovieDetailEvent.InvertMovieStarByIdDone ->
//                if (model.invertMovieStarByIdJob?.first == event.id) {
//                    Next.next(model.copy(invertMovieStarByIdJob = null))
//                } else {
//                    Next.noChange<MovieDetailModel, MovieDetailEffect>()
//                }
//
            is MovieDetailEvent.Error -> Next.next(model.copy(error = event.error))

            else -> Next.noChange<MovieDetailModel, MovieDetailEffect>()
        }
    }

    private val effectHandler = Connectable<MovieDetailEffect, MovieDetailEvent> { eventConsumer ->
        object : Connection<MovieDetailEffect> {
            override fun accept(effect: MovieDetailEffect) = when (effect) {
                is MovieDetailEffect.GetMovieById -> effectGetMovieById(effect.movieId, eventConsumer)

//
//                is MovieDetailEffect.InvertMovieStarById ->
//                    effectInvertMovieStarById(effect.invertMovieStarByIdJob, eventConsumer)
            }

            override fun dispose() = Unit  // We don't have any resources to release
        }
    }

    private fun effectGetMovieById(movieId: Int, eventConsumer: Consumer<MovieDetailEvent>) = runEffect {
        MoviesUseCase.MoviesUseCaseRequest.GetMovieByIdLiveData(movieId).process(moviesUseCase).onResult { result ->
            Timber.e("[${Utils.getLogPrefix(this)}] effectGetMovieById: $result")

            when (result) {
                is Result.InProgress -> Unit
                is Result.Success -> eventConsumer.accept(MovieDetailEvent.OnMovie(result.data))
                is Result.Error -> Unit
            }
        }
    }

    private lateinit var eventSourceChannel: SendChannel<MovieDetailEvent>

    private val eventSource = EventSource<MovieDetailEvent> { eventConsumer ->
        eventSourceChannel = actor(coroutineContext, Channel.UNLIMITED) {
            for (movieDetailEvent in this) eventConsumer.accept(movieDetailEvent)
        }

        Disposable { eventSourceChannel.close() }
    }

    private val loop: MobiusLoop<MovieDetailModel, MovieDetailEvent, MovieDetailEffect> =
        Mobius.loop(update, effectHandler)
            .effectRunner { WorkRunners.cachedThreadPool() } // TODO
            .eventSource(eventSource)
            .eventRunner { WorkRunners.cachedThreadPool() } //TODO
            .logger(AndroidLogger.tag("MovieDetailViewModel"))
            .startFrom(MovieDetailModel())
            .apply {
                observe { movieDetailModelLiveData.postValue(it) }
            }


    @MainThread
    fun onViewEvent(viewEvent: MovieDetailEvent) {
        Timber.e("[${Utils.getLogPrefix(this)}] onViewEvent: $viewEvent")
        loop.dispatchEvent(viewEvent)
    }

    override fun onException(exception: Throwable) {
        Timber.e("MovieDetailViewModel: [${Utils.getLogPrefix(this)}] Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
        eventSourceChannel.offer(MovieDetailEvent.Error(exception))
    }
}