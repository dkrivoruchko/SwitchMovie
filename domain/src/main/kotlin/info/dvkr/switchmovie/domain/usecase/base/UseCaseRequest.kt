package info.dvkr.switchmovie.domain.usecase.base

import kotlinx.coroutines.experimental.CompletableDeferred

open class UseCaseRequest<R> {
    @PublishedApi internal val resultDeffered: CompletableDeferred<Result<R>> = CompletableDeferred()

    suspend inline fun onResult(crossinline block: suspend (Result<R>) -> Unit) = block.invoke(resultDeffered.await())

    fun sendResult(result: Result<R>) = resultDeffered.complete(result)

    fun process(baseUseCase: BaseUseCase): UseCaseRequest<R> {
        try {
            val offerSuccessful = baseUseCase.offer(this)
            offerSuccessful || resultDeffered.complete(Result.Error(IllegalStateException("Channel is full: $baseUseCase")))
        } catch (ex: Exception) {
            resultDeffered.complete(Result.Error(ex, "UseCaseRequest.process:"))
        }

        return this
    }
}