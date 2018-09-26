package info.dvkr.switchmovie.data.viewmodel

import com.spotify.mobius.First
import com.spotify.mobius.MobiusLoop
import com.spotify.mobius.Next
import timber.log.Timber

class MobiusLogger<M, E, F>(tag: String, private val onException: (exception: Throwable) -> Unit) :
    MobiusLoop.Logger<M, E, F> {

    private val _tag: String = tag
        get() = "$field.${this.javaClass.simpleName}@${Thread.currentThread().name}"

    override fun beforeInit(model: M) {
        Timber.tag(_tag).d("Initializing loop")
    }

    override fun afterInit(model: M, result: First<M, F>) {
        Timber.tag(_tag).d("Loop initialized, starting from model: ${result.model()}")
        result.effects().forEach { effect -> Timber.tag(_tag).d("Effect dispatched: $effect") }
    }

    override fun exceptionDuringInit(model: M, exception: Throwable) {
        Timber.tag(_tag).e(exception, "Exception during initialization from model: $model")
        onException.invoke(exception)
    }

    override fun beforeUpdate(model: M, event: E) {
        Timber.tag(_tag).d("Event received: $event")
    }

    override fun afterUpdate(model: M, event: E, result: Next<M, F>) {
        if (result.hasModel()) Timber.tag(_tag).d("Model updated: ${result.modelUnsafe()}")
        result.effects().forEach { effect -> Timber.tag(_tag).d("Effect dispatched: $effect") }
    }

    override fun exceptionDuringUpdate(model: M, event: E, exception: Throwable) {
        Timber.tag(_tag).e(exception, "Exception updating model: $model with event: $event")
        onException.invoke(exception)
    }
}