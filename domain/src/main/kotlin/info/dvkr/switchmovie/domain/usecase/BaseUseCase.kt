package info.dvkr.switchmovie.domain.usecase

import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.flatMap
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow

abstract class BaseUseCase(protected val useCaseScope: CoroutineScope) {

    abstract class Request<R> {
        private val deferred: CompletableDeferred<Either<Throwable, R>> = CompletableDeferred()

        suspend fun process(baseUseCase: BaseUseCase): Either<Throwable, R> =
            Either<Throwable, Unit> { baseUseCase.enqueueForExecution(this@Request) }
                .flatMap { deferred.await() }
                .onFailure { deferred.complete(Either.Failure(it)) }

        fun sendResponse(response: Either<Throwable, R>) = deferred.complete(response)

        fun sendResponse(result: R) = deferred.complete(Either.Success(result))
    }

    protected abstract suspend fun onRequest(request: Request<*>)

    private val useCaseRequestChannel: Channel<Request<*>> = Channel(Channel.UNLIMITED)

    init {
        XLog.d(getLog("init"))

        useCaseScope.launch {
            useCaseRequestChannel.consumeAsFlow().collect { request ->
                ensureActive()
                try {
                    onRequest(request)
                } catch (exception: CancellationException) {
                    XLog.e(this@BaseUseCase.getLog("collect.exception"), exception)
                    throw exception
                } catch (th: Throwable) {
                    XLog.e(this@BaseUseCase.getLog("collect.th"), th)
                    request.sendResponse(Either.Failure(th))
                }
            }
        }
    }

    private fun enqueueForExecution(request: Request<*>) {
        XLog.v(getLog("enqueueForExecution", "Request: $request"))
        useCaseRequestChannel.offer(request)
    }
}