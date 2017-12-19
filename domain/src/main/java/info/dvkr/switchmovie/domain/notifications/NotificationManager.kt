package info.dvkr.switchmovie.domain.notifications

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.channels.SendChannel


interface NotificationManager {

  @Keep sealed class Event {
    @Keep data class OnMovieAdd(val movie: Movie) : Event()
    @Keep data class OnMovieUpdate(val movie: Movie) : Event()
  }

  fun offerEvent(event: Event)

  @Keep sealed class Subscription {
    @Keep data class OnMovieAdd(val channel: SendChannel<Notification>) : Subscription()
    @Keep data class OnMovieUpdate(val id: Int, val channel: SendChannel<Notification>) : Subscription()
  }

  fun subscribe(subscription: Subscription, owner: String)

  fun unSubscribe(subscription: Subscription, owner: String)

  fun unSubscribeAll(owner: String)

  @Keep sealed class Notification {
    @Keep data class OnMovieUpdate(val movie: Movie) : Notification()
  }
}