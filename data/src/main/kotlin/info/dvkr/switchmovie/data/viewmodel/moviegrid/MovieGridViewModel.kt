package info.dvkr.switchmovie.data.viewmodel.moviegrid

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
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import timber.log.Timber


class MovieGridViewModel(
    private val moviesUseCase: MoviesUseCase
) : BaseViewStateViewModel() {

    private val movieGridModelLiveData = MutableLiveData<MovieGridModel>()
    fun getMovieGridModelLiveData(): LiveData<MovieGridModel> = movieGridModelLiveData

    private val update = Update<MovieGridModel, MovieGridEvent, MovieGridEffect> { model, event ->
        Timber.e("[${Utils.getLogPrefix(this)}] Update: $event")

        return@Update when (event) {
            is MovieGridEvent.Init -> Next.dispatch(setOf(MovieGridEffect.Init))

            is MovieGridEvent.ViewCancelInvertMovieStarById ->
                if (model.invertMovieStarByIdJob?.first == event.movieId) {
                    model.invertMovieStarByIdJob.second.cancel()
                    Next.next(model.copy(invertMovieStarByIdJob = null))
                } else {
                    Next.noChange<MovieGridModel, MovieGridEffect>()
                }

            is MovieGridEvent.ViewRefresh -> Next.dispatch(setOf(MovieGridEffect.Refresh))

            is MovieGridEvent.ViewLoadMore -> Next.dispatch(setOf(MovieGridEffect.LoadMore))

            is MovieGridEvent.ViewInvertMovieStarById -> {
                val invertMovieStarByIdJob = Pair(event.movieId, Job())
                Next.next(
                    model.copy(invertMovieStarByIdJob = invertMovieStarByIdJob),
                    setOf(MovieGridEffect.InvertMovieStarById(invertMovieStarByIdJob))
                )
            }

            is MovieGridEvent.OnMovieList -> Next.next(model.copy(moviesLiveData = event.moviesLiveData))

            is MovieGridEvent.InvertMovieStarByIdDone ->
                if (model.invertMovieStarByIdJob?.first == event.movieId) {
                    Next.next(model.copy(invertMovieStarByIdJob = null))
                } else {
                    Next.noChange<MovieGridModel, MovieGridEffect>()
                }

            is MovieGridEvent.Error -> Next.next(model.copy(error = event.error))

            else -> Next.noChange<MovieGridModel, MovieGridEffect>()
        }
    }

    private val effectHandler = Connectable<MovieGridEffect, MovieGridEvent> { eventConsumer ->
        object : Connection<MovieGridEffect> {
            override fun accept(effect: MovieGridEffect) = when (effect) {
                MovieGridEffect.Init -> effectInit(eventConsumer)

                MovieGridEffect.Refresh -> effectRefresh(eventConsumer)

                MovieGridEffect.LoadMore -> effectLoadMore()

                is MovieGridEffect.InvertMovieStarById ->
                    effectInvertMovieStarById(effect.invertMovieStarByIdJob, eventConsumer)
            }

            override fun dispose() = Unit  // We don't have any resources to release
        }
    }

    private fun effectInit(eventConsumer: Consumer<MovieGridEvent>) = runEffect {
        MoviesUseCase.MoviesUseCaseRequest.GetMoviesLiveData().process(moviesUseCase).onResult { result ->
            Timber.e("[${Utils.getLogPrefix(this)}] effectInit: $result")

            when (result) {
                is Result.InProgress -> Unit
                is Result.Success -> eventConsumer.accept(MovieGridEvent.OnMovieList(result.data))
                is Result.Error -> Unit
            }
        }
    }

    private fun effectRefresh(eventConsumer: Consumer<MovieGridEvent>) = runEffect {
        MoviesUseCase.MoviesUseCaseRequest.ClearMovies().process(moviesUseCase).onResult { result ->
            Timber.e("[${Utils.getLogPrefix(this)}] effectRefresh: $result")

            when (result) {
                is Result.InProgress -> Unit
                is Result.Success -> eventConsumer.accept(MovieGridEvent.ViewLoadMore)
                is Result.Error -> Unit
            }
        }
    }

    private fun effectLoadMore() = runEffect {
        MoviesUseCase.MoviesUseCaseRequest.LoadMoreMovies().process(moviesUseCase).onResult { result ->
            Timber.e("[${Utils.getLogPrefix(this)}] effectLoadMore: $result")

            when (result) {
                is Result.InProgress -> Unit
                is Result.Success -> Unit
                is Result.Error -> Unit
            }
        }
    }

    private fun effectInvertMovieStarById(
        invertMovieStarByIdJob: Pair<Int, Job>,
        eventConsumer: Consumer<MovieGridEvent>
    ) {
        launch(coroutineContext + invertMovieStarByIdJob.second) {

            MoviesUseCase.MoviesUseCaseRequest.InvertMovieStarById(invertMovieStarByIdJob.first)
                .process(moviesUseCase)
                .onResult { result ->
                    Timber.e("[${Utils.getLogPrefix(this)}] effectStarMovieById: $result")

                    when (result) {
                        is Result.InProgress -> Unit
                        is Result.Success -> eventConsumer.accept(
                            MovieGridEvent.InvertMovieStarByIdDone(invertMovieStarByIdJob.first)
                        )
                        is Result.Error -> Unit
                    }
                }
        }
    }

    private lateinit var eventSourceChannel: SendChannel<MovieGridEvent>

    private val eventSource = EventSource<MovieGridEvent> { eventConsumer ->
        eventSourceChannel = actor(coroutineContext, Channel.UNLIMITED) {
            for (movieGridEvent in this) eventConsumer.accept(movieGridEvent)
        }

        Disposable { eventSourceChannel.close() }
    }

    private val loop: MobiusLoop<MovieGridModel, MovieGridEvent, MovieGridEffect> = Mobius.loop(update, effectHandler)
        .effectRunner { WorkRunners.cachedThreadPool() } // TODO
        .eventSource(eventSource)
        .eventRunner { WorkRunners.cachedThreadPool() } //TODO
        .logger(AndroidLogger.tag("MovieGridViewModel"))
        .startFrom(MovieGridModel())
        .apply {
            observe { movieGridModelLiveData.postValue(it) }
        }

    init {
        onViewEvent(MovieGridEvent.Init)
    }

    @MainThread
    fun onViewEvent(viewEvent: MovieGridEvent) {
        Timber.e("[${Utils.getLogPrefix(this)}] onViewEvent: $viewEvent")
        loop.dispatchEvent(viewEvent)
    }

    override fun onException(exception: Throwable) {
        Timber.e("MovieGridViewModel: [${Utils.getLogPrefix(this)}] Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
        eventSourceChannel.offer(MovieGridEvent.Error(exception))
    }
}