package info.dvkr.switchmovie.domain.usecase.base

import info.dvkr.switchmovie.domain.utils.Utils
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
        get() = parentJob + Dispatchers.Default + CoroutineExceptionHandler { _, exception ->
            Timber.e("BaseUseCase: [${Utils.getLogPrefix(this)}] Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
        }

    protected abstract val sendChannel: SendChannel<UseCaseRequest<*>>

    fun offer(request: UseCaseRequest<*>) = sendChannel.offer(request)
}