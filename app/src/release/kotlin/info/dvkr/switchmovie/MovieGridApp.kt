package info.dvkr.switchmovie

import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog

class MovieGridApp : BaseApp() {

    override fun initLogger() {
//        Fabric.with(this, Crashlytics())

        val logConfiguration = LogConfiguration.Builder()
            .logLevel(LogLevel.DEBUG)
            .tag("SSApp")
//            .throwableFormatter {
//                Crashlytics.logException(it)
//                StackTraceUtil.getStackTraceString(it)
//            }
//            .addInterceptor(object : AbstractFilterInterceptor() {
//                override fun reject(log: LogItem): Boolean {
//                    if (log.level >= LogLevel.DEBUG) Crashlytics.log(log.msg)
//                    return settingsReadOnly.loggingOn.not()
//                }
//            })
            .build()

        XLog.init(logConfiguration)
    }
}