package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

class MovieDetailPresenter internal constructor(private val movieRepository: MovieRepository) :
    BasePresenter<MovieDetailView, MovieDetailView.FromEvent>() {

  init {
    actor = actor(CommonPool, Channel.UNLIMITED) {
      for (fromEvent in this) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

        when (fromEvent) {
          is MovieDetailView.FromEvent.GetMovieById -> handleFromEvent(coroutineContext) {
            val response = CompletableDeferred<Movie>()
            movieRepository.send(MovieRepository.Request.GetMovieById(fromEvent.id, response))
            val movie = response.await()
            view?.toEvent(MovieDetailView.ToEvent.OnMovie(movie))
          }
        }
      }
    }
  }

  private fun handleFromEvent(coroutineContext: CoroutineContext,
                              code: suspend () -> Unit) = async(coroutineContext) {
    try {
      code()
    } catch (t: Throwable) {
      view?.toEvent(MovieDetailView.ToEvent.OnError(t))
    }
  }
}