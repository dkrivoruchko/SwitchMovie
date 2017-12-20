package info.dvkr.switchmovie.data.notifications

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.notifications.BaseNotificationManager
import kotlinx.coroutines.experimental.channels.SendChannel


interface NotificationManager: BaseNotificationManager {

  @Keep sealed class ChangeEvent : BaseNotificationManager.BaseChangeEvent() {
    @Keep data class OnMovieAdd(val movie: Movie) : ChangeEvent()
    @Keep data class OnMovieUpdate(val movie: Movie) : ChangeEvent()
  }

  @Keep sealed class Subscription : BaseNotificationManager.BaseSubscription() {
    @Keep data class OnMovieAdd(val channel: SendChannel<Notification>) : Subscription()
    @Keep data class OnMovieUpdate(val id: Int, val channel: SendChannel<Notification>) : Subscription()
  }

  @Keep sealed class Notification : BaseNotificationManager.BaseNotification() {
    @Keep data class OnMovieAdd(val movie: Movie) : Notification()
    @Keep data class OnMovieUpdate(val movie: Movie) : Notification()
  }
}