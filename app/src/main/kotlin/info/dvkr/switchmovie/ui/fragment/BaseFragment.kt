package info.dvkr.switchmovie.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.utils.getLog

abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        XLog.v(getLog("onViewCreated"))
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

    override fun onDestroyView() {
        XLog.v(getLog("onDestroyView"))
        super.onDestroyView()
    }
}