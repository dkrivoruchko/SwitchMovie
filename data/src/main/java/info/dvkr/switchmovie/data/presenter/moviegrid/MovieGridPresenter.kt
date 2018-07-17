package info.dvkr.switchmovie.data.presenter.moviegrid

import info.dvkr.switchmovie.data.notifications.NotificationManager
import info.dvkr.switchmovie.data.presenter.BasePresenter
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.UseCases
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class MovieGridPresenter(
    notificationManager: NotificationManager,
    private val useCases: UseCases
) : BasePresenter<MovieGridView, MovieGridView.FromEvent, MovieGridView.ToEvent>
    (notificationManager) {

    init {
        notificationChannel = actor(CommonPool, Channel.UNLIMITED) {
            for (notification in this) when (notification) {
                is NotificationManager.Notification.OnMovieUpdate -> onMovieUpdate(notification.movie)
            }
        }

        viewChannel = actor(CommonPool, Channel.UNLIMITED) {
            for (fromEvent in this) when (fromEvent) {
                MovieGridView.FromEvent.RefreshItems -> getMoviesFromIndex(0)
                is MovieGridView.FromEvent.GetNext -> getMoviesFromIndex(fromEvent.from)
                is MovieGridView.FromEvent.GetMovieById -> getMovieById(fromEvent.id)
                is MovieGridView.FromEvent.StarMovieById -> starMovieById(fromEvent.id)
            }
        }
    }

    private suspend fun getMoviesFromIndex(index: Int) = handleFromEvent(true) {
        val moviesOnRange = CompletableDeferred<UseCases.MoviesOnRange>().apply {
            useCases.send(UseCases.Request.GetMoviesFromIndex(index, this))
        }.await()

        val list = moviesOnRange.moviesList.map {
            MovieGridView.MovieGridItem(
                it.id,
                it.posterPath,
                it.isStar
            )
        }
        MovieGridView.ToEvent.OnMovieGridItemsRange(moviesOnRange.range, list)
    }

    private suspend fun getMovieById(id: Int) = handleFromEvent(true) {
        subscribeWithSwap(NotificationManager.Subscription.OnMovieUpdate(id, notificationChannel))

        val movie = CompletableDeferred<Movie>().apply {
            useCases.send(UseCases.Request.GetMovieById(id, this))
        }.await()

        MovieGridView.ToEvent.OnMovie(movie)
    }

    private suspend fun starMovieById(id: Int) = handleFromEvent(true) {
        val updatedMovieIndex = CompletableDeferred<Int>().apply {
            useCases.send(UseCases.Request.StarMovieById(id, this))
        }.await()

        offer(MovieGridView.FromEvent.GetNext(updatedMovieIndex))

        MovieGridView.ToEvent.OnStarMovieById(id)
    }

    private suspend fun onMovieUpdate(movie: Movie) = handleFromEvent {
        MovieGridView.ToEvent.OnMovie(movie)
    }
}