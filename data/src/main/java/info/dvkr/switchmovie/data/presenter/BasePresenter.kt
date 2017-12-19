package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

open class BasePresenter<T : BaseView, R : BaseView.BaseFromEvent, in S : BaseView.BaseToEvent>
  : ViewModel() {

  protected lateinit var sendChannel: SendChannel<R>
  protected var view: T? = null

  init {
    Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Init")
  }

  fun offer(fromEvent: R) {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] fromEvent: $fromEvent")
    sendChannel.offer(fromEvent)
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
    sendChannel.close()
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
}