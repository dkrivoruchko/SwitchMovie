package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import timber.log.Timber

class MovieDetailPresenter internal constructor(presenterContext: ThreadPoolDispatcher,
                                                private val movieRepository: MovieRepository)
  : BasePresenter<MovieDetailView, MovieDetailView.FromEvent>() {

  init {
    sendChannel = actor(presenterContext, Channel.UNLIMITED) {
      for (fromEvent in this) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

        when (fromEvent) {
          is MovieDetailView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
        }
      }
    }
  }

  private suspend fun handleFromEvent(code: suspend () -> Unit) =
      try {
        view?.toEvent(MovieDetailView.ToEvent.OnRefresh(true))
        code()
      } catch (t: Throwable) {
        view?.toEvent(MovieDetailView.ToEvent.OnError(t))
      } finally {
        view?.toEvent(MovieDetailView.ToEvent.OnRefresh(false))
      }

  private suspend fun getMovieById(id: Int) = handleFromEvent {
    val movie = movieRepository.get(MovieRepository.Request.GetMovieById(id))
    view?.toEvent(MovieDetailView.ToEvent.OnMovie(movie))
  }
}