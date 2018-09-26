package info.dvkr.switchmovie.data.viewmodel

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    @PublishedApi internal val parentJob: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default + CoroutineExceptionHandler { _, exception -> onException(exception) }

    protected abstract fun onException(exception: Throwable)

    @CallSuper
    override fun onCleared() {
        parentJob.cancel()
        super.onCleared()
    }
}