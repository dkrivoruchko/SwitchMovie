package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridView
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.notifications.NotificationManager
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class MovieDetailPresenter internal constructor(private val useCases: UseCases,
                                                private val notificationManager: NotificationManager)
  : BasePresenter<MovieDetailView, MovieDetailView.FromEvent, MovieDetailView.ToEvent>(notificationManager) {

  init {
    viewChannel = actor(CommonPool, Channel.UNLIMITED) {
      for (fromEvent in this) when (fromEvent) {
        is MovieDetailView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
      }
    }

    notificationChannel = actor(CommonPool, Channel.UNLIMITED) {
      for (notification in this) when (notification) {
        is NotificationManager.Notification.OnMovieUpdate -> onMovieUpdate(notification.movie)
      }
    }
  }

  private suspend fun getMovieById(id: Int) = handleFromEvent {
    val movie = useCases.get(UseCases.Case.GetMovieById(id))
    subscribe(NotificationManager.Subscription.OnMovieUpdate(movie.id, notificationChannel))
    MovieDetailView.ToEvent.OnMovie(movie)
  }

  private suspend fun onMovieUpdate(movie: Movie) = handleFromEvent {
    MovieDetailView.ToEvent.OnMovie(movie)
  }
}