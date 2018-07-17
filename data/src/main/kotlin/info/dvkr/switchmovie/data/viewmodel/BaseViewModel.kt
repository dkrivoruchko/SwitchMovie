package info.dvkr.switchmovie.data.viewmodel

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseViewModel : ViewModel(), CoroutineScope {

    @PublishedApi internal val parentJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default + CoroutineExceptionHandler { _, exception -> onException(exception) }

    abstract fun onException(exception: Throwable)



//    @MainThread
//    @CallSuper
//    open fun onViewEvent(viewEvent: BaseView.BaseViewEvent) {
//        Timber.e("[${Utils.getLogPrefix(this)}] onViewEvent: $viewEvent")
//    }



    @CallSuper
    override fun onCleared() {
        parentJob.cancel()
        super.onCleared()
    }
}