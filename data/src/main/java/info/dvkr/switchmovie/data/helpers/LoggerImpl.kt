package info.dvkr.switchmovie.data.helpers

import info.dvkr.switchmovie.domain.helpers.Logger
import timber.log.Timber

class LoggerImpl : Logger {
    override fun LogV(message: String) = Timber.v(message)
    override fun LogE(message: String) = Timber.e(message)
    override fun LogD(message: String) = Timber.d(message)
    override fun LogE(t: Throwable?, message: String?) = Timber.e(t, message)
}