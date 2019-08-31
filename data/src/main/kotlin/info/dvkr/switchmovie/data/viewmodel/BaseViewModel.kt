package info.dvkr.switchmovie.data.viewmodel

import androidx.annotation.AnyThread
import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive


abstract class BaseViewModel(viewModelScope: CoroutineScope) : ViewModel(), CoroutineScope by viewModelScope {

    interface Event
    interface State

    data class Error(val throwable: Throwable) : Event

    protected abstract suspend fun onEach(event: Event)

    protected abstract fun onError(throwable: Throwable)

    private val events = BroadcastChannel<Event>(64)

    init {
        events.asFlow()
                .onEach { event -> onEach(event) }
                .catch { throwable -> onError(throwable) }
                .launchIn(viewModelScope)
    }

    @AnyThread
    fun onEvent(event: Event) {
        XLog.d(getLog("onEvent", "$event"))

        if (coroutineContext.isActive.not()) {
            XLog.w(getLog("onEvent", "JobIsNotActive"))
            return
        }

        try {
            events.offer(event) || throw IllegalStateException("ChannelIsFull")
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

    protected val stateLiveData = MutableLiveData<State>()

    fun stateLiveData(): LiveData<State> = Transformations.distinctUntilChanged(stateLiveData)
}