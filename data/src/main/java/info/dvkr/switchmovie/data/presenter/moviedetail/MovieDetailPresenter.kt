package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.notifications.NotificationManager
import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class MovieDetailPresenter(
    notificationManager: NotificationManager,
    private val useCases: UseCases
) : BasePresenter<MovieDetailView, MovieDetailView.FromEvent, MovieDetailView.ToEvent>
    (notificationManager) {

    init {
        notificationChannel = actor(CommonPool, Channel.UNLIMITED) {
            for (notification in this) when (notification) {
                is NotificationManager.Notification.OnMovieUpdate -> onMovieUpdate(notification.movie)
            }
        }

        viewChannel = actor(CommonPool, Channel.UNLIMITED) {
            for (fromEvent in this) when (fromEvent) {
                is MovieDetailView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
            }
        }
    }

    private suspend fun getMovieById(id: Int) = handleFromEvent {
        subscribe(NotificationManager.Subscription.OnMovieUpdate(id, notificationChannel))
        subscribeWithSwap(NotificationManager.Subscription.OnMovieUpdate(id, notificationChannel))

        val movie = CompletableDeferred<Movie>().apply {
            useCases.send(UseCases.Request.GetMovieById(id, this))
        }.await()

        MovieDetailView.ToEvent.OnMovie(movie)
    }

    private suspend fun onMovieUpdate(movie: Movie) = handleFromEvent {
        MovieDetailView.ToEvent.OnMovie(movie)
    }
}