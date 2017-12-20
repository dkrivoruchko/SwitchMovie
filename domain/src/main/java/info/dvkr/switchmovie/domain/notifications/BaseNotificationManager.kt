package info.dvkr.switchmovie.domain.notifications

import android.support.annotation.Keep


interface BaseNotificationManager {

  @Keep open class BaseEvent
  @Keep open class BaseSubscription
  @Keep open class BaseNotification

  fun offerEvent(event: BaseEvent)

  fun subscribe(baseSubscription: BaseSubscription, owner: String)

  fun unSubscribe(baseSubscription: BaseSubscription, owner: String)

  fun unSubscribeAll(owner: String)
}