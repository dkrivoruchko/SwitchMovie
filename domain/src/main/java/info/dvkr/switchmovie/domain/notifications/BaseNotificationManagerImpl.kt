package info.dvkr.switchmovie.domain.notifications

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

open class BaseNotificationManagerImpl : BaseNotificationManager {
  @Volatile
  private var subscriptions: List<Pair<BaseNotificationManager.BaseSubscription, String>> = emptyList()

  private val subscriptionsMutex = Mutex()

  protected lateinit var eventChannel: SendChannel<BaseNotificationManager.BaseEvent>

  override fun offerEvent(event: BaseNotificationManager.BaseEvent) {
    System.out.println("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] event: $event")
    eventChannel.offer(event)
  }

  override fun subscribe(baseSubscription: BaseNotificationManager.BaseSubscription, owner: String) {
    launch {
      subscriptionsMutex.withLock {
        subscriptions
            .toMutableList()
            .apply { add(Pair(baseSubscription, owner)) }
            .toList()
            .run { subscriptions = this }
      }
    }
  }

  override fun unSubscribe(baseSubscription: BaseNotificationManager.BaseSubscription, owner: String) {
    launch {
      subscriptionsMutex.withLock {
        subscriptions
            .asSequence()
            .filterNot {
              it.first.javaClass.canonicalName == baseSubscription.javaClass.canonicalName &&
                  it.second == owner
            }
            .toList()
            .run { subscriptions = this }
      }
    }
  }

  override fun unSubscribeAll(owner: String) {
    launch {
      subscriptionsMutex.withLock {
        subscriptions
            .asSequence()
            .filter { it.second != owner }
            .toList()
            .run { subscriptions = this }
      }
    }
  }

  fun notify(code: suspend (subscription: BaseNotificationManager.BaseSubscription) -> Unit) = async(CommonPool) {
    subscriptionsMutex.withLock {
      subscriptions.forEach {
        code(it.first)
      }
    }
  }
}