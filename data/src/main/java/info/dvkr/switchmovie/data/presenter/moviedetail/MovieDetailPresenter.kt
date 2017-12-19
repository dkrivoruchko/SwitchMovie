package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class MovieDetailPresenter internal constructor(private val useCases: UseCases)
  : BasePresenter<MovieDetailView, MovieDetailView.FromEvent, MovieDetailView.ToEvent>() {

  init {
    sendChannel = actor(CommonPool, Channel.UNLIMITED) {
      for (fromEvent in this) when (fromEvent) {
        is MovieDetailView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
      }
    }
  }

  private suspend fun getMovieById(id: Int) = handleFromEvent {
    val movie = useCases.get(UseCases.Case.GetMovieById(id))
    MovieDetailView.ToEvent.OnMovie(movie)
  }
}