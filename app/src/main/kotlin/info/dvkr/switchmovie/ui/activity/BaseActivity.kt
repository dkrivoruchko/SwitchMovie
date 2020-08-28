package info.dvkr.switchmovie.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog

abstract class BaseActivity(@LayoutRes contentLayoutId: Int) : AppCompatActivity(contentLayoutId) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XLog.v(getLog("onCreate"))
    }

    override fun onNewIntent(intent: Intent?) {
        XLog.d(getLog("onNewIntent", "Intent: $intent"))
        super.onNewIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        XLog.v(getLog("onStart"))
    }

    override fun onResume() {
        super.onResume()
        XLog.v(getLog("onResume"))
    }

    override fun onPause() {
        XLog.v(getLog("onPause"))
        super.onPause()
    }

    override fun onStop() {
        XLog.v(getLog("onStop"))
        super.onStop()
    }

    override fun onDestroy() {
        XLog.v(getLog("onDestroy"))
        super.onDestroy()
    }
}