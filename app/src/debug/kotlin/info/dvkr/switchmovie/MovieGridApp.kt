package info.dvkr.switchmovie

import android.os.StrictMode
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.squareup.leakcanary.LeakCanary

class MovieGridApp : BaseApp() {

    override fun initLogger() {
//        System.setProperty("kotlinx.coroutines.debug", "on")

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

        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)

        val logConfiguration = LogConfiguration.Builder().tag("SSApp").build()
        XLog.init(logConfiguration, AndroidPrinter())
    }
}