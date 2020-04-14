package info.dvkr.switchmovie.viewmodel

import androidx.annotation.AnyThread
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.actor


abstract class BaseViewModel(protected val viewModelScope: CoroutineScope) : ViewModel() {

    interface Event

    data class Error(val throwable: Throwable) : Event

    protected abstract suspend fun onEach(event: Event)
    protected abstract fun onError(throwable: Throwable)

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        XLog.e(getLog("onCoroutineException"), throwable)
        onError(throwable)
    }

    private val eventsChannel by lazy {
        viewModelScope.actor<Event>(capacity = 16) {
            for (event in this) {
                try {
                    if (event is Error) onError(event.throwable) else onEach(event)
                } catch (ignore: CancellationException) {
                    XLog.w(this@BaseViewModel.getLog("actor.ignore"), ignore)
                } catch (th: Throwable) {
                    XLog.e(this@BaseViewModel.getLog("actor"), th)
                    onError(th)
                }
            }
        }
    }

    @AnyThread
    fun onEvent(event: Event) {
        XLog.d(getLog("onEvent", "$event"))
        try {
            eventsChannel.isClosedForSend.not() || throw IllegalStateException("ChannelIsClosed")
            eventsChannel.offer(event) || throw IllegalStateException("ChannelIsFull")
        } catch (ignore: CancellationException) {
            XLog.e(getLog("onEvent.ignore", "CancellationException"))// Possible?
        } catch (th: Throwable) {
            XLog.e(getLog("onEvent"), th)
        }
    }


    @CallSuper
    override fun onCleared() {
        XLog.d(getLog("onCleared"))
        viewModelScope.cancel(CancellationException(getLog("onCleared")))
        super.onCleared()
    }
}