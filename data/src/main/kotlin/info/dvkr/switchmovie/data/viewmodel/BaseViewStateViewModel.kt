package info.dvkr.switchmovie.data.viewmodel

import android.support.annotation.CallSuper
import android.support.annotation.MainThread
import com.spotify.mobius.EventSource
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.disposables.Disposable
import info.dvkr.switchmovie.domain.utils.getTag
import kotlinx.coroutines.experimental.CoroutineName
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

abstract class BaseViewStateViewModel<M, E, F> : BaseViewModel() {

    protected abstract val loop: MobiusLoop<M, E, F>

    protected inline fun runEffect(job: Job = Job(parentJob), crossinline block: suspend () -> Unit) {
        launch(coroutineContext + job + CoroutineName("runEffect")) { block.invoke() }
    }

    private lateinit var eventSourceChannel: SendChannel<E>

    protected val eventSource = EventSource<E> { eventConsumer ->
        eventSourceChannel = actor(coroutineContext + CoroutineName("eventSourceActor"), capacity = 8) {
            for (eventSource in this) {
                Timber.tag(getTag("eventSource")).d(eventSource.toString())
                eventConsumer.accept(eventSource)
            }
        }

        Disposable { eventSourceChannel.close() }
    }

    protected fun sendEventInternal(event: E) {
        try {
            val offerSuccessful = eventSourceChannel.offer(event)
            offerSuccessful || throw IllegalStateException("Channel is full: $eventSourceChannel")
        } catch (ex: Exception) {
            Timber.tag(getTag("sendEventInternal")).e(ex)
        }
    }

    @MainThread
    @CallSuper
    fun onViewEvent(viewEvent: E) {
        Timber.tag(getTag("onViewEvent")).d(viewEvent.toString())
        loop.dispatchEvent(viewEvent)
    }

    @CallSuper
    override fun onException(exception: Throwable) {
        Timber.tag(getTag("onException"))
            .e(exception, "Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
    }
}