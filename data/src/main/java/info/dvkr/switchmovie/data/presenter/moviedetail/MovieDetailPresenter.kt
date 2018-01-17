package info.dvkr.switchmovie.data.presenter.moviedetail

import info.dvkr.switchmovie.data.notifications.NotificationManager
import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlin.coroutines.experimental.CoroutineContext

class MovieDetailPresenter(crtContext: CoroutineContext,
                           notificationManager: NotificationManager,
                           private val useCases: UseCases)
    : BasePresenter<MovieDetailView, MovieDetailView.FromEvent, MovieDetailView.ToEvent>
(crtContext, notificationManager) {

    init {
        viewChannel = actor(crtContext, Channel.UNLIMITED) {
            for (fromEvent in this) when (fromEvent) {
                is MovieDetailView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
            }
        }

        notificationChannel = actor(crtContext, Channel.UNLIMITED) {
            for (notification in this) when (notification) {
                is NotificationManager.Notification.OnMovieUpdate -> onMovieUpdate(notification.movie)
            }
        }
    }

    private suspend fun getMovieById(id: Int) = handleFromEvent {
        subscribe(NotificationManager.Subscription.OnMovieUpdate(id, notificationChannel))
        val movie = useCases.get(UseCases.Case.GetMovieById(id))
        MovieDetailView.ToEvent.OnMovie(movie)
    }

    private suspend fun onMovieUpdate(movie: Movie) = handleFromEvent {
        MovieDetailView.ToEvent.OnMovie(movie)
    }
}