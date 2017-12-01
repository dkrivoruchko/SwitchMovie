package info.dvkr.switchmovie

import android.app.Application
import android.os.StrictMode
import android.util.Log
import com.squareup.leakcanary.LeakCanary
import info.dvkr.switchmovie.di.KoinModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class MovieGridApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set up Timber
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashReportingTree())
        Timber.i("[${Thread.currentThread().name}] onCreate")

        // Turning on strict mode
        if (BuildConfig.DEBUG_MODE) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }

        // Set up LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)

        // Set up DI
        startKoin(this, listOf(KoinModule()))
    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) return
//            Crashlytics.log(priority, tag, message)
//            t?.let { Crashlytics.logException(it) }
        }
    }
}