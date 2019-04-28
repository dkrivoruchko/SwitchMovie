package info.dvkr.switchmovie

import android.app.Application
import com.elvishew.xlog.XLog
import com.jakewharton.threetenabp.AndroidThreeTen
import info.dvkr.switchmovie.di.apiKoinModule
import info.dvkr.switchmovie.di.baseKoinModule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.conscrypt.Conscrypt
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.core.context.startKoin
import java.security.Security

abstract class BaseApp : Application() {
    abstract fun initLogger()

    override fun onCreate() {
        super.onCreate()

        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        startKoin {
            androidContext(this@BaseApp)
            androidFileProperties()
            modules(baseKoinModule, apiKoinModule)
        }

        initLogger()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            XLog.e("Uncaught throwable in thread ${thread.name}", throwable)
            defaultHandler.uncaughtException(thread, throwable)
        }

        GlobalScope.launch { AndroidThreeTen.init(this@BaseApp) }
    }

}