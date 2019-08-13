package info.dvkr.switchmovie.domain.usecase.base

import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.flatMap
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

abstract class BaseUseCase(useCaseScope: CoroutineScope) : CoroutineScope by useCaseScope {

    private val useCaseRequestChannel = BroadcastChannel<Request<*>>(16)

    protected abstract suspend fun requestHandler(event: Request<*>)

    init {
        useCaseRequestChannel.asFlow()
            .onEach {
                try {
                    requestHandler(it)
                } catch (th: Throwable) {
                    it.sendResponse(Either.Failure(th))
                }
            }
            .launchIn(useCaseScope)
    }

    private fun enqueueForExecution(request: Request<*>) {
        XLog.d(getLog("enqueueForExecution", "Request: $request"))

        coroutineContext.isActive || throw IllegalStateException("JobIsNotActive")
        useCaseRequestChannel.offer(request) || throw IllegalStateException("ChannelIsFull")
    }

    abstract class Request<R> {
        private val resultDeffered: CompletableDeferred<Either<Throwable, R>> = CompletableDeferred()
        private lateinit var runOnStart: () -> Any

        fun onStart(block: () -> Any): Request<R> = this.apply { runOnStart = block }

        suspend fun process(baseUseCase: BaseUseCase): Either<Throwable, R> =
            Either<Throwable, Unit> {
                if (::runOnStart.isInitialized) runOnStart.invoke()
                baseUseCase.enqueueForExecution(this@Request)
            }
                .flatMap { resultDeffered.await() }
                .onFailure { sendResponse(Either.Failure(it)) }

        fun sendResponse(response: Either<Throwable, R>) = resultDeffered.complete(response)
    }

//    inline fun <reified R> Request<R>.sendResponse(crossinline block: () -> R) = this.sendResponse(Either { block() })

    suspend inline fun <reified R> Request<R>.sendResponse(crossinline block: suspend () -> R) =
        this.sendResponse(Either { block() })
}