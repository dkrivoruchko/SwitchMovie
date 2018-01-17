package info.dvkr.switchmovie.data.notifications

import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.notifications.BaseNotificationManager
import info.dvkr.switchmovie.domain.notifications.BaseNotificationManagerImpl
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor


class NotificationManagerImpl : BaseNotificationManagerImpl(), NotificationManager {

    init {
        changeEventChannel = actor(CommonPool, Channel.UNLIMITED) {
            for (notification in this) when (notification) {
                is NotificationManager.ChangeEvent.OnMovieAdd -> notifyMovieAdd(notification.movie)
                is NotificationManager.ChangeEvent.OnMovieUpdate -> notifyMovieUpdate(notification.movie)
            }
        }
    }

    private suspend fun notifyMovieAdd(movie: Movie) {
    }

    private suspend fun notifyMovieUpdate(movie: Movie) = notify { subscription ->
        if (subscription is NotificationManager.Subscription.OnMovieUpdate) {
            if (!subscription.channel.isClosedForSend && subscription.id == movie.id)
                subscription.channel.send(NotificationManager.Notification.OnMovieUpdate(movie))
        }
    }
}