package info.dvkr.switchmovie.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.dvkr.switchmovie.domain.utils.getTag
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext

abstract class BaseActivity : AppCompatActivity(), CoroutineScope {

    protected lateinit var parentJob: Job

    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main + CoroutineExceptionHandler { _, exception ->
            Timber.tag(getTag("onException"))
                .e(exception, "Caught $exception with suppressed ${exception.suppressed?.contentToString()}")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(getTag("onCreate")).d("Invoked")
        parentJob = Job()
    }

    override fun onDestroy() {
        Timber.tag(getTag("onDestroy")).d("Invoked")
        parentJob.cancel()
        super.onDestroy()
    }
}