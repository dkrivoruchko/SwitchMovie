package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

open class MovieGridPresenter internal constructor(private val movieRepository: MovieRepository) :
    BasePresenter<MovieGridView, MovieGridView.FromEvent>() {

  private val viewCache: MutableMap<String, MutableList<MovieGridView.ToEvent>> = mutableMapOf()

  init {
    actor = actor(CommonPool, Channel.UNLIMITED) {
      for (fromEvent in this) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

        when (fromEvent) {
          is MovieGridView.FromEvent.GetCache -> {
//                        for (mutableEntry in viewCache) {
//                            Timber.e("Sending from cache: ${mutableEntry.key}: ${mutableEntry.value}")
//                            mutableEntry.value.forEach { view?.toEvent(it) }
//                        }
          }

          is MovieGridView.FromEvent.RefreshItems -> handleFromEvent(coroutineContext) {
            val response = CompletableDeferred<MovieRepository.MoviesOnRange>()
            movieRepository.send(MovieRepository.Request.GetMoviesFromIndex(0, response))
            val moviesOnRange = response.await()
            val list = moviesOnRange.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath, it.isStar) }
            view?.toEvent(MovieGridView.ToEvent.OnMovieGridItemsRange(moviesOnRange.range, list))
          }

          is MovieGridView.FromEvent.GetNext -> handleFromEvent(coroutineContext) {
            val response = CompletableDeferred<MovieRepository.MoviesOnRange>()
            movieRepository.send(MovieRepository.Request.GetMoviesFromIndex(fromEvent.from, response))
            val moviesOnRange = response.await()
            val list = moviesOnRange.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath, it.isStar) }
            view?.toEvent(MovieGridView.ToEvent.OnMovieGridItemsRange(moviesOnRange.range, list))
          }

          is MovieGridView.FromEvent.GetMovieById -> handleFromEvent(coroutineContext) {
            val response = CompletableDeferred<Movie>()
            movieRepository.send(MovieRepository.Request.GetMovieById(fromEvent.id, response))
            val movie = response.await()
            view?.toEvent(MovieGridView.ToEvent.OnMovie(movie))
          }

          is MovieGridView.FromEvent.StarMovieById -> handleFromEvent(coroutineContext) {
            delay(5000)
            val response = CompletableDeferred<Int>()
            movieRepository.send(MovieRepository.Request.StarMovieById(fromEvent.id, response))
            val updatedMovieIndex = response.await()
            actor.offer(MovieGridView.FromEvent.GetNext(updatedMovieIndex))
            view?.toEvent(MovieGridView.ToEvent.OnStarMovieById(fromEvent.id))
          }
        }
      }
    }
  }

  private fun handleFromEvent(coroutineContext: CoroutineContext,
                              code: suspend () -> Unit) = async(coroutineContext) {
    view?.toEvent(MovieGridView.ToEvent.OnRefresh(true))
    try {
      code()
    } catch (t: Throwable) {
      view?.toEvent(MovieGridView.ToEvent.OnError(t))
    } finally {
      view?.toEvent(MovieGridView.ToEvent.OnRefresh(false))
    }
  }

//    private fun notifyView(response: MovieGridView.ToEvent, keyClass: Any = response, append: Boolean = false) {
//        Timber.e("Put to cache: ${keyClass.javaClass.canonicalName}: $response")
//
//        val cacheValuesList = viewCache[keyClass.javaClass.canonicalName]
//        if (append && cacheValuesList != null) {
//            viewCache.put(keyClass.javaClass.canonicalName, cacheValuesList.apply { add(response) })
//        } else {
//            viewCache.put(keyClass.javaClass.canonicalName, mutableListOf(response))
//        }
//        view?.toEvent(response)
//    }

}