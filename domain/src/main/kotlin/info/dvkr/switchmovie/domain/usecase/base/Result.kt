package info.dvkr.switchmovie.domain.usecase.base

sealed class Result<out R> {

    data class Success<out T>(val data: T) : Result<T>() {
        override fun toString() = "Success[data=$data]"
    }

    data class Error(val exception: Exception, val message: String = "") : Result<Nothing>() {
        override fun toString() = "Error[$message $exception]"
    }

    object InProgress : Result<Nothing>() {
        override fun toString() = "InProgress"
    }
}