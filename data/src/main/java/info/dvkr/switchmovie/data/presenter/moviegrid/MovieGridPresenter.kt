package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

open class MovieGridPresenter internal constructor(presenterContext: ThreadPoolDispatcher,
                                                   private val movieRepository: MovieRepository) :
        BasePresenter<MovieGridView, MovieGridView.FromEvent>() {

    private val viewCache: MutableMap<String, MutableList<MovieGridView.ToEvent>> = mutableMapOf()

    init {
        actor = actor(presenterContext, Channel.UNLIMITED) {
            for (fromEvent in this) {
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

                when (fromEvent) {
                    is MovieGridView.FromEvent.GetCache -> {
                        for (mutableEntry in viewCache) {
                            Timber.e("Sending from cache: ${mutableEntry.key}: ${mutableEntry.value}")
                            mutableEntry.value.forEach { view?.toEvent(it) }
                        }
                    }

                    is MovieGridView.FromEvent.RefreshItems -> {
                        notifyView(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.offer(MovieRepository.Action.GetMoviesFromIndex(0))
                    }

                    is MovieGridView.FromEvent.GetNext -> {
                        notifyView(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.offer(MovieRepository.Action.GetMoviesFromIndex(fromEvent.from))
                    }

                    is MovieGridView.FromEvent.GetMovieById -> {
                        movieRepository.offer(MovieRepository.Action.GetMovieById(fromEvent.id))
                    }

                    is MovieGridView.FromEvent.StarMovieById -> {
                        notifyView(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.offer(MovieRepository.Action.StarMovieById(fromEvent.id))
                    }
                }
            }
        }

        job = launch(presenterContext) {
            movieRepository.subscribe().consumeEach { result ->
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] result: $result")

                when (result) {
                    is MovieRepository.Result.MoviesOnRange -> {
                        val list = result.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath, it.isStar) }
                        notifyView(MovieGridView.ToEvent.OnMovieGridItemsRange(result.range, list), result, true)
                        notifyView(MovieGridView.ToEvent.OnRefresh(false))
                    }

                    is MovieRepository.Result.Error -> {
                        notifyView(MovieGridView.ToEvent.OnError(result.error), result)
                        notifyView(MovieGridView.ToEvent.OnRefresh(false))
                    }

                    is MovieRepository.Result.MovieById -> {
                        notifyView(MovieGridView.ToEvent.OnMovie(result.movie), result)
                    }
                }
            }
        }
    }

    private fun notifyView(response: MovieGridView.ToEvent, keyClass: Any = response, append: Boolean = false) {
        Timber.e("Put to cache: ${keyClass.javaClass.canonicalName}: $response")

        val cacheValuesList = viewCache[keyClass.javaClass.canonicalName]
        if (append && cacheValuesList != null) {
            viewCache.put(keyClass.javaClass.canonicalName, cacheValuesList.apply { add(response) })
        } else {
            viewCache.put(keyClass.javaClass.canonicalName, mutableListOf(response))
        }
        view?.toEvent(response)
    }

}