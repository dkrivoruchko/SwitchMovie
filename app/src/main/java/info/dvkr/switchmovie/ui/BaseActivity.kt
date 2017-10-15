package info.dvkr.switchmovie.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import info.dvkr.switchmovie.MovieGridApp
import info.dvkr.switchmovie.dagger.component.NonConfigurationComponent

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var injector: NonConfigurationComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedInjector = lastCustomNonConfigurationInstance
        injector = if (null == savedInjector) {
            (application as MovieGridApp).appComponent().plusActivityComponent()
        } else {
            savedInjector as NonConfigurationComponent
        }

        inject(injector)
    }

    abstract fun inject(injector: NonConfigurationComponent)

    override fun onRetainCustomNonConfigurationInstance(): Any = injector
}