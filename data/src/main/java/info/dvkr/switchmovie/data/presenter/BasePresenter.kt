package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import android.support.annotation.MainThread
import info.dvkr.switchmovie.data.notifications.NotificationManager
import info.dvkr.switchmovie.domain.notifications.BaseNotificationManager
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

open class BasePresenter<in T : BaseView, R : BaseView.BaseFromEvent, in S : BaseView.BaseToEvent>
constructor(protected val notificationManager: NotificationManager) : ViewModel() {

    protected lateinit var viewChannel: SendChannel<R>
    protected lateinit var notificationChannel: SendChannel<NotificationManager.Notification>

    private var view: T? = null

    protected val isWorkInProgress = AtomicInteger(0)

    init {
        Timber.i("[${Utils.getLogPrefix(this)}] Init")
    }

    @MainThread
    fun offer(fromEvent: R) {
        Timber.d("[${Utils.getLogPrefix(this)}] fromEvent: ${fromEvent.javaClass.simpleName}")
        viewChannel.offer(fromEvent)
    }

    @CallSuper
    open fun attach(newView: T) {
        Timber.i("[${Utils.getLogPrefix(this)}] Attach")
        view?.let { detach() }
        view = newView

        notifyView(BaseView.BaseToEvent.OnProgress(isWorkInProgress.get() > 0))
    }

    @CallSuper
    open fun detach() {
        Timber.i("[${Utils.getLogPrefix(this)}] Detach")
        view = null
    }

    @CallSuper
    override fun onCleared() {
        viewChannel.close()
        notificationManager.updateSubscription(
            BaseNotificationManager.BaseSubscriptionEvent.UnSubscribeAll(this.javaClass.canonicalName)
        )
        notificationChannel.close()
        super.onCleared()
    }

    protected suspend fun handleFromEvent(
        showWorkInProgress: Boolean = false,
        code: suspend () -> S
    ) = async {
        try {
            if (showWorkInProgress) {
                notifyView(BaseView.BaseToEvent.OnProgress(isWorkInProgress.incrementAndGet() > 0))
            }

            notifyView(code())
        } catch (t: Throwable) {
            Timber.e(t)
            notifyView(BaseView.BaseToEvent.OnError(t))
        } finally {
            if (showWorkInProgress) {
                notifyView(BaseView.BaseToEvent.OnProgress(isWorkInProgress.decrementAndGet() > 0))
            }
        }
    }

    protected fun <E : BaseView.BaseToEvent> notifyView(baseToEvent: E) = view?.toEvent(baseToEvent)

    protected suspend fun subscribe(subscription: BaseNotificationManager.BaseSubscription) {
        notificationManager.updateSubscription(
            BaseNotificationManager.BaseSubscriptionEvent.Subscribe(
                subscription,
                this.javaClass.canonicalName
            )
        )
    }

    protected suspend fun subscribeWithSwap(subscription: BaseNotificationManager.BaseSubscription) {
        notificationManager.updateSubscription(
            BaseNotificationManager.BaseSubscriptionEvent.UnSubscribe(
                subscription,
                this.javaClass.canonicalName
            )
        )
        subscribe(subscription)
    }
}