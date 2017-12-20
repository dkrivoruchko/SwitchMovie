package info.dvkr.switchmovie.data.notifications

import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.notifications.NotificationManager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference


class NotificationManagerImpl : NotificationManager {
  private val subscriptions: AtomicReference<List<Pair<NotificationManager.Subscription, String>>> =
      AtomicReference(emptyList())

  private val subscriptionsMutex = Mutex()

  private val eventChannel = actor<NotificationManager.Event>(CommonPool, Channel.UNLIMITED) {
    for (notification in this) when (notification) {
      is NotificationManager.Event.OnMovieAdd -> notifyMovieAdd(notification.movie)
      is NotificationManager.Event.OnMovieUpdate -> notifyMovieUpdate(notification.movie)
    }
  }

  override fun offerEvent(event: NotificationManager.Event) {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] event: $event")
    eventChannel.offer(event)
  }

  override fun subscribe(subscription: NotificationManager.Subscription, owner: String) {
    launch {
      subscriptionsMutex.withLock {
        subscriptions.get()
            .toMutableList()
            .apply { add(Pair(subscription, owner)) }
            .toList()
            .run { subscriptions.set(this) }
      }
    }
  }

  override fun unSubscribe(subscription: NotificationManager.Subscription, owner: String) {
    launch {
      subscriptionsMutex.withLock {
        subscriptions.get()
            .asSequence()
            .filterNot {
              it.first.javaClass.canonicalName == subscription.javaClass.canonicalName &&
                  it.second == owner
            }
            .toList()
            .run { subscriptions.set(this) }
      }
    }
  }

  override fun unSubscribeAll(owner: String) {
    launch {
      subscriptionsMutex.withLock {
        subscriptions.get()
            .asSequence()
            .filter { it.second != owner }
            .toList()
            .run { subscriptions.set(this) }
      }
    }
  }


  private suspend fun notifyMovieAdd(movie: Movie) {
  }

  private suspend fun notifyMovieUpdate(movie: Movie) = async(CommonPool) {
    subscriptionsMutex.withLock {
      subscriptions.get()
          .forEach {
            val subscription = it.first
            if (subscription is NotificationManager.Subscription.OnMovieUpdate) {
              if (!subscription.channel.isClosedForSend && subscription.id == movie.id)
                subscription.channel.send(NotificationManager.Notification.OnMovieUpdate(movie))
            }
          }
    }
  }
}