package info.dvkr.switchmovie.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CoroutineExceptionHandler

abstract class BaseActivity : AppCompatActivity() {

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        XLog.e(getLog("onCoroutineException"), throwable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XLog.d(getLog("onCreate"))
    }

    override fun onDestroy() {
        XLog.d(getLog("onDestroy"))
        super.onDestroy()
    }
}