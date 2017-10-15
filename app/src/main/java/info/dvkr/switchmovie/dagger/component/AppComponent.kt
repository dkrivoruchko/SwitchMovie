package info.dvkr.switchmovie.dagger.component

import dagger.Component
import info.dvkr.switchmovie.MovieGridApp
import info.dvkr.switchmovie.dagger.module.AppModule
import info.dvkr.switchmovie.data.dagger.module.ApiModule
import info.dvkr.switchmovie.data.dagger.module.LocalModule
import info.dvkr.switchmovie.data.dagger.module.RepositoryModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
        AppModule::class,
        ApiModule::class,
        LocalModule::class,
        RepositoryModule::class
))
interface AppComponent {
    fun plusActivityComponent(): NonConfigurationComponent

    fun inject(movieGridApp: MovieGridApp)
}