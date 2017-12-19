package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridView
import info.dvkr.switchmovie.domain.notifications.NotificationManager
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

open class BasePresenter<T : BaseView, R : BaseView.BaseFromEvent, in S : BaseView.BaseToEvent>
constructor(private val notificationManager: NotificationManager) : ViewModel() {

  protected lateinit var viewChannel: SendChannel<R>
  protected lateinit var notificationChannel: SendChannel<NotificationManager.Notification>

  protected var view: T? = null

  init {
    Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Init")
  }

  fun offer(fromEvent: R) {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")
    viewChannel.offer(fromEvent)
  }

  open fun attach(newView: T) {
    Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Attach")
    view?.let { detach() }
    view = newView
  }

  open fun detach() {
    Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Detach")
    view = null
  }

  override fun onCleared() {
    viewChannel.close()
    notificationManager.unSubscribeAll(this.javaClass.canonicalName)
    notificationChannel.close()
    super.onCleared()
  }

  protected suspend fun handleFromEvent(showRefresh: Boolean = false,
                                        code: suspend () -> S) = async {
    try {
      if (showRefresh) notifyView(BaseView.BaseToEvent.OnRefresh(true))
      notifyView(code())
    } catch (t: Throwable) {
      notifyView(MovieGridView.ToEvent.OnError(t))
    } finally {
      if (showRefresh) notifyView(BaseView.BaseToEvent.OnRefresh(false))
    }
  }

  private fun <K : BaseView.BaseToEvent> notifyView(baseToEvent: K)
      = launch(UI) { view?.toEvent(baseToEvent) }


  protected suspend fun subscribe(subscription: NotificationManager.Subscription,
                                  unSubscribeCurrent: Boolean = true) {

    if (unSubscribeCurrent) notificationManager.unSubscribe(subscription, this.javaClass.canonicalName)
    notificationManager.subscribe(subscription, this.javaClass.canonicalName)
  }
}