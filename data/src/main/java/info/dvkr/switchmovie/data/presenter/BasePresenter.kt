package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ActorJob
import timber.log.Timber

open class BasePresenter<T, R> : ViewModel() {

    protected lateinit var actor: ActorJob<R>
    protected lateinit var job: Job
    protected var view: T? = null

    init {
        Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Init")
    }

    fun offer(fromEvent: R) = actor.offer(fromEvent)

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
        actor.channel.close()
        job.cancel()
        super.onCleared()
    }
}