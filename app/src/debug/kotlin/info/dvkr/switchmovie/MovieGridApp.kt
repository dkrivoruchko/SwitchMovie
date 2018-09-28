package info.dvkr.switchmovie

import android.app.Application
import android.os.StrictMode
import com.datatheorem.android.trustkit.TrustKit
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary
import info.dvkr.switchmovie.di.apiKoinModule
import info.dvkr.switchmovie.di.baseKoinModule
import org.koin.android.ext.android.startKoin
import org.koin.log.Logger
import timber.log.Timber

class MovieGridApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set up Timber
        Timber.plant(Timber.DebugTree())

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            Timber.e(throwable, "Uncaught throwable in thread ${thread.name}")
            defaultHandler.uncaughtException(thread, throwable)
        }

        // Turning on strict mode
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads()
                .permitDiskWrites()
                .penaltyLog()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        // Set up LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)

//        System.setProperty("kotlinx.coroutines.debug", "on")

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