package info.dvkr.switchmovie.viewmodel

import androidx.annotation.AnyThread
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch


abstract class BaseViewModel<T> : ViewModel() {

    interface Event

    data class Error(val throwable: Throwable) : Event

    @MainThread
    protected abstract fun onError(throwable: Throwable)

    @MainThread
    protected abstract suspend fun onEach(event: Event)

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        XLog.e(getLog("onCoroutineException"), throwable)
        onError(throwable)
    }

    protected val stateLiveData = MutableLiveData<T>()

    fun getStateLiveData(): LiveData<T> = Transformations.distinctUntilChanged(stateLiveData)

    private val viewModelEventChannel: Channel<Event> = Channel(Channel.UNLIMITED)

    init {
        XLog.d(getLog("init"))

        viewModelScope.launch {
            viewModelEventChannel.consumeAsFlow().collect { event ->
                ensureActive()
                try {
                    when (event) {
                        is Error -> onError(event.throwable)
                        else -> onEach(event)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (th: Throwable) {
                    XLog.e(this@BaseViewModel.getLog("collect"), th)
                    onError(th)
                }
            }
        }
    }

    @AnyThread
    fun onEvent(event: Event) {
        XLog.d(getLog("onEvent", "$event"))
        if (viewModelEventChannel.isClosedForSend) {
            XLog.w(getLog("onEvent", "isClosedForSend"))
            return
        }

        try {
            viewModelEventChannel.offer(event)
        } catch (th: Throwable) {
            XLog.e(getLog("onEvent"), th)
        }
    }

    @CallSuper
    override fun onCleared() {
        XLog.d(getLog("onCleared"))
        viewModelEventChannel.close()
        super.onCleared()
    }

}