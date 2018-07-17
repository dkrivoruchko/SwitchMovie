package info.dvkr.switchmovie

import android.app.Application
import android.util.Log
import info.dvkr.switchmovie.di.apiKoinModule
import info.dvkr.switchmovie.di.baseKoinModule
import info.dvkr.switchmovie.di.databaseKoinModule
import info.dvkr.switchmovie.domain.utils.Utils
import org.koin.android.ext.android.startKoin
import org.koin.log.Logger
import timber.log.Timber

class MovieGridApp : Application() {

    private object CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return
//            Crashlytics.log(priority, tag, message)
//            error?.let { Crashlytics.logException(it) }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Set up Timber
        Timber.plant(CrashReportingTree)
        Timber.v("[${Utils.getLogPrefix(this)}] onCreate")

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            Timber.e(throwable, "Uncaught throwable in thread ${thread.name}")
            defaultHandler.uncaughtException(thread, throwable)
        }

        // Set up DI
        startKoin(this,
            listOf(baseKoinModule, apiKoinModule, databaseKoinModule),
            loadProperties = true,
            logger = object : Logger {
                override fun debug(msg: String) = Timber.d("Koin: $msg")
                override fun err(msg: String) = Timber.e("Koin: $msg")
                override fun info(msg: String) = Timber.i("Koin: $msg")
            })
    }
}