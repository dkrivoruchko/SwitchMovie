package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

open class MovieGridPresenter internal constructor(private val useCases: UseCases)
  : BasePresenter<MovieGridView, MovieGridView.FromEvent, MovieGridView.ToEvent>() {

  init {
    sendChannel = actor(CommonPool, Channel.UNLIMITED) {
      for (fromEvent in this) when (fromEvent) {
        is MovieGridView.FromEvent.RefreshItems -> getMoviesFromIndex(0)
        is MovieGridView.FromEvent.GetNext -> getMoviesFromIndex(fromEvent.from)
        is MovieGridView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
        is MovieGridView.FromEvent.StarMovieById -> starMovieById(fromEvent.id)
      }
    }
  }

  private suspend fun getMoviesFromIndex(index: Int) = handleFromEvent(true) {
    val moviesOnRange = useCases.get(UseCases.Case.GetMoviesFromIndex(index))
    val list = moviesOnRange.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath, it.isStar) }
    MovieGridView.ToEvent.OnMovieGridItemsRange(moviesOnRange.range, list)
  }

  private suspend fun getMovieById(id: Int) = handleFromEvent(true) {
    val movie = useCases.get(UseCases.Case.GetMovieById(id))
    MovieGridView.ToEvent.OnMovie(movie)
  }

  private suspend fun starMovieById(id: Int) = handleFromEvent(true) {
    val updatedMovieIndex = useCases.get(UseCases.Case.StarMovieById(id))
    sendChannel.offer(MovieGridView.FromEvent.GetNext(updatedMovieIndex))
    MovieGridView.ToEvent.OnStarMovieById(id)
  }
}