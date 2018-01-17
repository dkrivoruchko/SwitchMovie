package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.notifications.NotificationManager
import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlin.coroutines.experimental.CoroutineContext

class MovieGridPresenter(crtContext: CoroutineContext,
                         notificationManager: NotificationManager,
                         private val useCases: UseCases)
    : BasePresenter<MovieGridView, MovieGridView.FromEvent, MovieGridView.ToEvent>
(crtContext, notificationManager) {

    init {
        viewChannel = actor(crtContext, Channel.UNLIMITED) {
            for (fromEvent in this) when (fromEvent) {
                MovieGridView.FromEvent.RefreshItems -> getMoviesFromIndex(0)
                is MovieGridView.FromEvent.GetNext -> getMoviesFromIndex(fromEvent.from)
                is MovieGridView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
                is MovieGridView.FromEvent.StarMovieById -> starMovieById(fromEvent.id)
            }
        }

        notificationChannel = actor(crtContext, Channel.UNLIMITED) {
            for (notification in this) when (notification) {
                is NotificationManager.Notification.OnMovieUpdate -> onMovieUpdate(notification.movie)
            }
        }
    }

    private suspend fun getMoviesFromIndex(index: Int) = handleFromEvent(true) {
        val moviesOnRange = useCases.get(UseCases.Case.GetMoviesFromIndex(index))
        val list = moviesOnRange.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath, it.isStar) }
        MovieGridView.ToEvent.OnMovieGridItemsRange(moviesOnRange.range, list)
    }

    private suspend fun getMovieById(id: Int) = handleFromEvent(true) {
        subscribeWithSwap(NotificationManager.Subscription.OnMovieUpdate(id, notificationChannel))
        val movie = useCases.get(UseCases.Case.GetMovieById(id))
        MovieGridView.ToEvent.OnMovie(movie)
    }

    private suspend fun starMovieById(id: Int) = handleFromEvent(true) {
        val updatedMovieIndex = useCases.get(UseCases.Case.StarMovieById(id))
        viewChannel.offer(MovieGridView.FromEvent.GetNext(updatedMovieIndex))
        MovieGridView.ToEvent.OnStarMovieById(id)
    }

    private suspend fun onMovieUpdate(movie: Movie) = handleFromEvent {
        MovieGridView.ToEvent.OnMovie(movie)
    }
}