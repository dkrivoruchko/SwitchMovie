package info.dvkr.switchmovie.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import info.dvkr.switchmovie.data.presenter.PresenterFactory
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity() {
    protected val presenterFactory: PresenterFactory by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] onCreate")
    }

    override fun onDestroy() {
        Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] onDestroy")
        super.onDestroy()
    }
}