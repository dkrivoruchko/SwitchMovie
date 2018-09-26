package info.dvkr.switchmovie

import android.app.Application
import android.util.Log
import com.datatheorem.android.trustkit.TrustKit
import com.jakewharton.threetenabp.AndroidThreeTen
import info.dvkr.switchmovie.di.apiKoinModule
import info.dvkr.switchmovie.di.baseKoinModule
import org.koin.android.ext.android.startKoin
import org.koin.log.Logger
import timber.log.Timber

class MovieGridApp : Application() {

    private object CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Set up Timber
        Timber.plant(CrashReportingTree)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            Timber.e(throwable, "Uncaught throwable in thread ${thread.name}")
            defaultHandler.uncaughtException(thread, throwable)
        }

        AndroidThreeTen.init(this)

        TrustKit.initializeWithNetworkSecurityConfiguration(this)

        // Set up DI
        startKoin(this,
            listOf(baseKoinModule, apiKoinModule),
            loadProperties = true,
            logger = object : Logger {
                override fun debug(msg: String) = Timber.tag("Koin").d(msg)
                override fun err(msg: String) = Timber.tag("Koin").e(msg)
                override fun info(msg: String) = Timber.tag("Koin").i(msg)
            })
    }
}