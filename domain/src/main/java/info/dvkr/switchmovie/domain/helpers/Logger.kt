package info.dvkr.switchmovie.domain.helpers


interface Logger {
    fun LogV(message: String)
    fun LogD(message: String)
    fun LogE(message: String)
    fun LogE(t: Throwable?, message: String?)
}