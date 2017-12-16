package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import timber.log.Timber

open class MovieGridPresenter internal constructor(presenterContext: ThreadPoolDispatcher,
                                                   private val movieRepository: MovieRepository)
  : BasePresenter<MovieGridView, MovieGridView.FromEvent>() {

  init {
    sendChannel = actor(presenterContext, Channel.UNLIMITED) {
      for (fromEvent in this) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")

        when (fromEvent) {
          is MovieGridView.FromEvent.RefreshItems -> getMoviesFromIndex(0)
          is MovieGridView.FromEvent.GetNext -> getMoviesFromIndex(fromEvent.from)
          is MovieGridView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
          is MovieGridView.FromEvent.StarMovieById -> starMovieById(fromEvent.id)
        }
      }
    }
  }

  private suspend fun handleFromEvent(code: suspend () -> Unit) =
      try {
        view?.toEvent(MovieGridView.ToEvent.OnRefresh(true))
        code()
      } catch (t: Throwable) {
        view?.toEvent(MovieGridView.ToEvent.OnError(t))
      } finally {
        view?.toEvent(MovieGridView.ToEvent.OnRefresh(false))
      }

  private suspend fun getMoviesFromIndex(index: Int) = handleFromEvent {
    val moviesOnRange = movieRepository.get(MovieRepository.Request.GetMoviesFromIndex(index))
    val list = moviesOnRange.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath, it.isStar) }
    view?.toEvent(MovieGridView.ToEvent.OnMovieGridItemsRange(moviesOnRange.range, list))
  }

  private suspend fun getMovieById(id: Int) = handleFromEvent {
    val movie = movieRepository.get(MovieRepository.Request.GetMovieById(id))
    view?.toEvent(MovieGridView.ToEvent.OnMovie(movie))
  }

  private suspend fun starMovieById(id: Int) = handleFromEvent {
    val updatedMovieIndex = movieRepository.get(MovieRepository.Request.StarMovieById(id))
    sendChannel.offer(MovieGridView.FromEvent.GetNext(updatedMovieIndex))
    view?.toEvent(MovieGridView.ToEvent.OnStarMovieById(id))
  }
}