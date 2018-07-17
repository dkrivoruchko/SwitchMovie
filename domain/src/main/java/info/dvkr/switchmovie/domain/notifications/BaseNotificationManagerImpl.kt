package info.dvkr.switchmovie.domain.notifications

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor

open class BaseNotificationManagerImpl : BaseNotificationManager {

    protected lateinit var changeEventChannel: SendChannel<BaseNotificationManager.BaseChangeEvent>

    override fun offerChangeEvent(baseChangeEvent: BaseNotificationManager.BaseChangeEvent) {
        System.out.println("[${Utils.getLogPrefix(this)}] baseChangeEvent: $baseChangeEvent")
        changeEventChannel.offer(baseChangeEvent)
    }

    private val subscriptionChannel = actor<BaseNotificationManager.BaseSubscriptionEvent> {
        val subscriptions: MutableList<Pair<BaseNotificationManager.BaseSubscription, String>> =
            mutableListOf()

        for (subscriptionEvent in channel) when (subscriptionEvent) {

            is BaseNotificationManager.BaseSubscriptionEvent.Subscribe -> {
                subscriptions.add(Pair(subscriptionEvent.baseSubscription, subscriptionEvent.owner))
            }

            is BaseNotificationManager.BaseSubscriptionEvent.UnSubscribe -> {
                val iterator = subscriptions.iterator()
                while (iterator.hasNext()) {
                    val pair = iterator.next()
                    if (pair.first.javaClass.canonicalName == subscriptionEvent.baseSubscription.javaClass.canonicalName &&
                        pair.second == subscriptionEvent.owner) {
                        iterator.remove()
                    }
                }
            }

            is BaseNotificationManager.BaseSubscriptionEvent.UnSubscribeAll -> {
                val iterator = subscriptions.iterator()
                while (iterator.hasNext()) {
                    val pair = iterator.next()
                    if (pair.second == subscriptionEvent.owner) iterator.remove()
                }
            }

            is GetSubscriptions -> {
                subscriptionEvent.response.complete(subscriptions.toList())
            }
        }
    }

    override fun updateSubscription(baseSubscriptionEvent: BaseNotificationManager.BaseSubscriptionEvent) {
        System.out.println("[${Utils.getLogPrefix(this)}] baseSubscriptionEvent: $baseSubscriptionEvent")
        subscriptionChannel.offer(baseSubscriptionEvent)
    }

    @Keep private data class GetSubscriptions(
        val response: CompletableDeferred<List<Pair<BaseNotificationManager.BaseSubscription, String>>>
    ) : BaseNotificationManager.BaseSubscriptionEvent()

    fun notify(code: suspend (subscription: BaseNotificationManager.BaseSubscription) -> Unit) =
        async(CommonPool) {
            val response =
                CompletableDeferred<List<Pair<BaseNotificationManager.BaseSubscription, String>>>()
            subscriptionChannel.offer(GetSubscriptions(response))
            val subscriptions = response.await()
            subscriptions.forEach { code(it.first) }
        }
}