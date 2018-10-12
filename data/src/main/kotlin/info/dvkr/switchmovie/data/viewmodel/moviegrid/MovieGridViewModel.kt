package info.dvkr.switchmovie.data.viewmodel.moviegrid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.spotify.mobius.*
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.runners.WorkRunner
import info.dvkr.switchmovie.data.viewmodel.BaseViewStateViewModel
import info.dvkr.switchmovie.data.viewmodel.MobiusLogger
import info.dvkr.switchmovie.domain.usecase.MoviesUseCase
import info.dvkr.switchmovie.domain.utils.getTag
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import timber.log.Timber


class MovieGridViewModel(
    private val eventWorkRunner: WorkRunner,
    private val effectWorkRunner: WorkRunner,
    private val moviesUseCase: MoviesUseCase
) : BaseViewStateViewModel<MovieGridModel, MovieGridEvent, MovieGridEffect>() {

    private val movieGridModelLiveData = MutableLiveData<MovieGridModel>()
    fun getMovieGridModelLiveData(): LiveData<MovieGridModel> = movieGridModelLiveData

    private val update = Update<MovieGridModel, MovieGridEvent, MovieGridEffect> { model, event ->
        return@Update when (event) {
            MovieGridEvent.Init -> Next.dispatch(setOf(MovieGridEffect.Init))

            is MovieGridEvent.ViewCancelInvertMovieStarById ->
                if (model.invertMovieStarByIdJob?.first == event.movieId) {
                    model.invertMovieStarByIdJob.second.cancel()
                    Next.next(model.copy(invertMovieStarByIdJob = null))
                } else {
                    Next.noChange<MovieGridModel, MovieGridEffect>()
                }

            is MovieGridEvent.ViewRefresh -> Next.dispatch(setOf(MovieGridEffect.Refresh))
            is MovieGridEvent.ViewUpdate -> Next.dispatch(setOf(MovieGridEffect.Update))
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

            MovieGridEvent.WorkStart -> Next.next(model.copy(workInProgressCounter = model.workInProgressCounter + 1))

            MovieGridEvent.WorkFinish -> {
                val workInProgress = model.workInProgressCounter - 1
                workInProgress >= 0 || throw IllegalStateException("workInProgressCounter: $workInProgress < 0")
                Next.next(model.copy(workInProgressCounter = workInProgress))
            }

            is MovieGridEvent.Error -> Next.next(model.copy(error = event.error))

            else -> throw IllegalStateException("Unknown MovieGridEvent: $event")
        }
    }

    private val effectHandler = Connectable<MovieGridEffect, MovieGridEvent> { eventConsumer ->
        object : Connection<MovieGridEffect> {
            override fun accept(effect: MovieGridEffect) {
                when (effect) {
                    MovieGridEffect.Init -> runEffect { effectInit(eventConsumer) }

                    MovieGridEffect.Refresh -> runEffect {
                        eventConsumer.accept(MovieGridEvent.WorkStart)
                        effectRefresh(eventConsumer)
                        eventConsumer.accept(MovieGridEvent.WorkFinish)
                    }

                    MovieGridEffect.Update -> runEffect {
                        eventConsumer.accept(MovieGridEvent.WorkStart)
                        effectUpdate(eventConsumer)
                        eventConsumer.accept(MovieGridEvent.WorkFinish)
                    }

                    MovieGridEffect.LoadMore -> runEffect {
                        eventConsumer.accept(MovieGridEvent.WorkStart)
                        effectLoadMore()
                        eventConsumer.accept(MovieGridEvent.WorkFinish)
                    }

                    is MovieGridEffect.InvertMovieStarById -> runEffect(effect.invertMovieStarByIdJob.second) {
                        effectInvertMovieStarById(effect.invertMovieStarByIdJob, eventConsumer)
                    }
                }
            }

            override fun dispose() = Unit  // We don't have any resources to release
        }
    }

    private suspend fun effectInit(eventConsumer: Consumer<MovieGridEvent>) =
        MoviesUseCase.Request.GetMoviesLiveData().process(moviesUseCase) { resultEither ->
            Timber.tag(getTag("effectInit")).d(resultEither.toString())

            resultEither.either(
                { sendEventInternal(MovieGridEvent.Error(it)) },
                { eventConsumer.accept(MovieGridEvent.OnMovieList(it)) }
            )
        }

    private suspend fun effectRefresh(eventConsumer: Consumer<MovieGridEvent>) =
        MoviesUseCase.Request.ClearMovies().process(moviesUseCase) { resultEither ->
            Timber.tag(getTag("effectRefresh")).d(resultEither.toString())

            resultEither.either(
                { sendEventInternal(MovieGridEvent.Error(it)) },
                { eventConsumer.accept(MovieGridEvent.ViewLoadMore) }
            )
        }

    private suspend fun effectUpdate(eventConsumer: Consumer<MovieGridEvent>) =
        MoviesUseCase.Request.UpdateMovies().process(moviesUseCase) { resultEither ->
            Timber.tag(getTag("effectUpdate")).d(resultEither.toString())

            resultEither.either(
                {},
                { eventConsumer.accept(MovieGridEvent.ViewLoadMore) }
            )
        }

    private suspend fun effectLoadMore() =
        MoviesUseCase.Request.LoadMoreMovies().process(moviesUseCase) { resultEither ->
            Timber.tag(getTag("effectLoadMore")).d(resultEither.toString())

            resultEither.either(
                { sendEventInternal(MovieGridEvent.Error(it)) },
                {}
            )
        }

    private suspend fun effectInvertMovieStarById(
        invertMovieStarByIdJob: Pair<Int, Job>,
        eventConsumer: Consumer<MovieGridEvent>
    ) {
        delay(3000)
        MoviesUseCase.Request.InvertMovieStarById(invertMovieStarByIdJob.first).process(moviesUseCase) { resultEither ->
            Timber.tag(getTag("effectInvertMovieStarById")).d(resultEither.toString())

            resultEither.either(
                { sendEventInternal(MovieGridEvent.Error(it)) },
                { eventConsumer.accept(MovieGridEvent.InvertMovieStarByIdDone(invertMovieStarByIdJob.first)) }
            )
        }
    }

    override val loop: MobiusLoop<MovieGridModel, MovieGridEvent, MovieGridEffect> =
        Mobius.loop(update, effectHandler)
            .eventSource(eventSource)
            .eventRunner { eventWorkRunner }
            .effectRunner { effectWorkRunner }
            .logger(MobiusLogger(this.javaClass.simpleName, ::onException))
            .startFrom(MovieGridModel())
            .apply {
                observe { movieGridModelLiveData.postValue(it) }
            }

    override fun onException(exception: Throwable) {
        super.onException(exception)
        sendEventInternal(MovieGridEvent.Error(exception))
    }

    init {
        onViewEvent(MovieGridEvent.Init)
    }
}