package info.dvkr.switchmovie.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.dvkr.switchmovie.data.viewmodel.BaseView
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseActivity : AppCompatActivity(), BaseView, CoroutineScope {

    protected lateinit var parentJob: Job
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("[${Utils.getLogPrefix(this)}] onCreate")
        parentJob = Job()
    }

    override fun onDestroy() {
        parentJob.cancel()
        Timber.i("[${Utils.getLogPrefix(this)}] onDestroy")
        super.onDestroy()
    }
}