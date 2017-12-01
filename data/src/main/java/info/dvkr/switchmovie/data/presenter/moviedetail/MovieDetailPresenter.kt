package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class MovieDetailPresenter internal constructor(presenterContext: ThreadPoolDispatcher,
                                                private val movieRepository: MovieRepository) :
        BasePresenter<MovieDetailView, MovieDetailView.FromEvent>() {

    init {
        actor = actor(presenterContext, Channel.UNLIMITED) {
            for (fromEvent in this) {
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

                when (fromEvent) {
                    is MovieDetailView.FromEvent.GetMovieById -> {
                        movieRepository.offer(MovieRepository.Action.GetMovieById(fromEvent.id))
                    }
                }
            }
        }

        job = launch(presenterContext) {
            movieRepository.subscribe().consumeEach { result ->
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] result: $result")

                when (result) {
                    is MovieRepository.Result.MovieById -> {
                        view?.toEvent(MovieDetailView.ToEvent.OnMovie(result.movie))
                    }

                    is MovieRepository.Result.Error -> {
                        view?.toEvent(MovieDetailView.ToEvent.OnError(result.error))
                    }
                }
            }
        }
    }
}