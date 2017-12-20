package info.dvkr.switchmovie.domain.notifications

import android.support.annotation.Keep
import kotlinx.coroutines.experimental.CompletableDeferred


interface BaseNotificationManager {

  @Keep open class BaseChangeEvent

  fun offerChangeEvent(baseChangeEvent: BaseChangeEvent)

  @Keep open class BaseNotification

  @Keep open class BaseSubscription

  @Keep open class BaseSubscriptionEvent {
    @Keep data class Subscribe(val baseSubscription: BaseSubscription, val owner: String) : BaseSubscriptionEvent()
    @Keep data class UnSubscribe(val baseSubscription: BaseSubscription, val owner: String) : BaseSubscriptionEvent()
    @Keep data class UnSubscribeAll(val owner: String) : BaseSubscriptionEvent()
  }

  fun updateSubscription(baseSubscriptionEvent: BaseSubscriptionEvent)
}