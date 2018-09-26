package info.dvkr.switchmovie.domain.usecase.base

import android.support.annotation.CallSuper
import info.dvkr.switchmovie.domain.utils.getTag
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.SendChannel
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseUseCase : CoroutineScope {

    private val parentJob: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default + CoroutineExceptionHandler { _, exception -> onException(exception) }

    @CallSuper
    protected fun onException(exception: Throwable) {
        Timber.tag(getTag())
            .e(exception, "Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
    }

    protected abstract val sendChannel: SendChannel<BaseUseCaseRequest<*>>

    @Throws(Exception::class)
    fun offer(request: BaseUseCaseRequest<*>) = sendChannel.offer(request)
}