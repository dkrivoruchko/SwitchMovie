package info.dvkr.switchmovie.data.viewmodel

import androidx.annotation.AnyThread
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel


abstract class BaseViewModel(viewModelScope: CoroutineScope) : ViewModel(), CoroutineScope by viewModelScope {

    interface Event

    data class Error(val throwable: Throwable) : Event

    internal abstract val viewModelEventChannel: SendChannel<Event>

    @AnyThread
    fun onEvent(event: Event) {
        XLog.d(getLog("onEvent", "$event"))

        if (coroutineContext.isActive.not()) {
            XLog.w(getLog("sendEvent", "JobIsNotActive"))
            return
        }

        try {
            viewModelEventChannel.offer(event) || throw IllegalStateException("ChannelIsFull")
        } catch (th: Throwable) {
            XLog.e(getLog("onEvent"), th)
        }
    }

    @CallSuper
    override fun onCleared() {
        XLog.d(getLog("onCleared", "Invoked"))
        coroutineContext.cancelChildren()
        super.onCleared()
    }
}