package info.dvkr.switchmovie.domain.usecase.base

import info.dvkr.switchmovie.domain.utils.Either
import kotlinx.coroutines.experimental.CompletableDeferred

open class BaseUseCaseRequest<R> {
    @PublishedApi internal val resultDeffered: CompletableDeferred<Either<Throwable, R>> = CompletableDeferred()

    fun sendResult(resultEither: Either<Throwable, R>) = resultDeffered.complete(resultEither)

    suspend inline fun process(
        baseUseCase: BaseUseCase,
        crossinline onResult: suspend (Either<Throwable, R>) -> Unit
    ) {
        try {
            val offerSuccessful = baseUseCase.offer(this)
            offerSuccessful || throw IllegalStateException("Channel is full: $baseUseCase")
            onResult(resultDeffered.await())
        } catch (ex: Exception) {
            resultDeffered.complete(Either.Left(ex))
        }
    }
}