package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class MovieGridPresenter internal constructor(private val presenterContext: ThreadPoolDispatcher,
                                              private val movieRepository: MovieRepository) :
        BasePresenter<MovieGridView, MovieGridView.FromEvent>() {

    init {
        actor = actor(presenterContext, Channel.UNLIMITED) {
            for (fromEvent in this) {
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

                when (fromEvent) {
                    is MovieGridView.FromEvent.RefreshItems -> {
                        view?.toEvent(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.offer(MovieRepository.Action.GetMoviesOnPage(1))
                    }

                    is MovieGridView.FromEvent.GetPage -> {
                        view?.toEvent(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.offer(MovieRepository.Action.GetMoviesOnPage(fromEvent.page))
                    }
                }
            }
        }

        job = launch(presenterContext) {
            movieRepository.subscribe().consumeEach { result ->
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] result: $result")

                when (result) {
                    is MovieRepository.Result.Movies -> {
                        val list = result.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath) }
                        view?.toEvent(MovieGridView.ToEvent.OnMovieGridItemsRefresh(list))
                        view?.toEvent(MovieGridView.ToEvent.OnRefresh(false))
                    }

                    is MovieRepository.Result.MoviesOnPage -> {
                        val list = result.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath) }
                        view?.toEvent(MovieGridView.ToEvent.OnMovieGridItemsPage(list))
                        view?.toEvent(MovieGridView.ToEvent.OnRefresh(false))
                    }

                    is MovieRepository.Result.Error -> {
                        view?.toEvent(MovieGridView.ToEvent.OnError(result.error))
                        view?.toEvent(MovieGridView.ToEvent.OnRefresh(false))
                    }
                }
            }
        }
    }
}