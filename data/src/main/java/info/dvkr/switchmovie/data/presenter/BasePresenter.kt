package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.channels.SendChannel
import timber.log.Timber

open class BasePresenter<T, R> : ViewModel() {

  protected lateinit var sendChannel: SendChannel<R>
  protected var view: T? = null

  init {
    Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Init")
  }

  fun offer(fromEvent: R) = sendChannel.offer(fromEvent)

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
}