package info.dvkr.switchmovie

import android.app.Application
import info.dvkr.switchmovie.di.apiKoinModule
import info.dvkr.switchmovie.di.baseKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

abstract class BaseApp : Application() {
    abstract fun initLogger()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@BaseApp)
            modules(listOf(baseKoinModule, apiKoinModule))
        }

        initLogger()

//        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
//        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
//            XLog.e("Uncaught throwable in thread ${thread.name}", throwable)
//            defaultHandler?.uncaughtException(thread, throwable)
//        }
    }

}