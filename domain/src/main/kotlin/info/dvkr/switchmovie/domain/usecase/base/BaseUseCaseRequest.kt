package info.dvkr.switchmovie.domain.usecase.base

import info.dvkr.switchmovie.domain.utils.Either
import kotlinx.coroutines.experimental.CompletableDeferred

open class BaseUseCaseRequest<R> {
    @PublishedApi internal val resultDeffered: CompletableDeferred<Either<Throwable, R>> = CompletableDeferred()

    suspend inline fun onResult(crossinline block: suspend (Either<Throwable, R>) -> Unit) =
        block.invoke(resultDeffered.await())

    fun sendResult(resultEither: Either<Throwable, R>) = resultDeffered.complete(resultEither)

    fun process(baseUseCase: BaseUseCase): BaseUseCaseRequest<R> {
        try {
            val offerSuccessful = baseUseCase.offer(this)
            offerSuccessful || throw IllegalStateException("Channel is full: $baseUseCase")
        } catch (ex: Exception) {
            resultDeffered.complete(Either.Left(ex))
        }

        return this
    }
}