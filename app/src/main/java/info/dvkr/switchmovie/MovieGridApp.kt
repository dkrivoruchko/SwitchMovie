package info.dvkr.switchmovie

import android.app.Application
import info.dvkr.switchmovie.dagger.component.AppComponent
import info.dvkr.switchmovie.dagger.component.DaggerAppComponent
import info.dvkr.switchmovie.dagger.module.AppModule

class MovieGridApp : Application() {
    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    fun appComponent(): AppComponent = appComponent
}