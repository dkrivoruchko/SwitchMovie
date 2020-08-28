package info.dvkr.switchmovie.domain.utils


sealed class Either<out F : Throwable, out S> {
    data class Failure<out F : Throwable>(val exception: F) : Either<F, Nothing>() {
        companion object {
            operator fun <F : Throwable> invoke(f: F): Either<F, Nothing> = Failure(f)
        }
    }

    data class Success<out S>(val value: S) : Either<Nothing, S>() {
        companion object {
            operator fun <S> invoke(s: S): Either<Nothing, S> = Success(s)
        }
    }

    val isFailure get() = this is Failure<F>
    val isSuccess get() = this is Success<S>

    companion object {
        inline operator fun <reified F : Throwable, reified S> invoke(block: () -> S): Either<F, S> =
            try {
                Success(block())
            } catch (e: Throwable) {
                Failure(e as F)
            }
    }

    inline fun <reified T : Any> fold(crossinline fnF: (F) -> T, crossinline fnS: (S) -> T): T = when (this) {
        is Failure -> fnF(exception)
        is Success -> fnS(value)
    }

    inline fun onSuccess(crossinline fnS: (S) -> Any): Either<F, S> = when (this) {
        is Failure -> this
        is Success -> {
            fnS(value)
            this
        }
    }

    suspend inline fun onSuccessSuspend(crossinline fnS: suspend (S) -> Any): Either<F, S> = when (this) {
        is Failure -> this
        is Success -> {
            fnS(value)
            this
        }
    }

    inline fun onFailure(crossinline fnF: (F) -> Any): Either<F, S> = when (this) {
        is Failure -> {
            fnF(exception)
            this
        }
        is Success -> this
    }

    suspend inline fun onFailureSuspend(crossinline fnF: suspend (F) -> Any): Either<F, S> = when (this) {
        is Failure -> {
            fnF(exception)
            this
        }
        is Success -> this
    }

    inline fun onAny(crossinline fn: (Either<F, S>) -> Any): Either<F, S> = apply { fn(this) }
}

inline fun <T, F : Throwable, S> Either<F, S>.flatMap(fn: (S) -> Either<F, T>): Either<F, T> =
    when (this) {
        is Either.Failure -> this
        is Either.Success -> fn(value)
    }

inline fun <T, F : Throwable, S> Either<F, S>.map(fnS: (S) -> (T)): Either<F, T> =
    when (this) {
        is Either.Failure -> this
        is Either.Success -> Either.Success(fnS(value))
    }

inline fun <T : Throwable, F : Throwable, S> Either<F, S>.mapFailure(fnF: (F) -> (T)): Either<T, S> =
    when (this) {
        is Either.Failure -> Either.Failure(fnF(this.exception))
        is Either.Success -> this
    }