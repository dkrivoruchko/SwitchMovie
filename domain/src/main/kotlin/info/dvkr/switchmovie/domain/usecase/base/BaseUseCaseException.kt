package info.dvkr.switchmovie.domain.usecase.base

import android.support.annotation.Keep


sealed class BaseUseCaseException : Exception() {
    @Keep data class NetworkException(val ex: Throwable) : BaseUseCaseException()
}