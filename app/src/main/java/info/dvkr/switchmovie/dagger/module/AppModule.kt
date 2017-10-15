package info.dvkr.switchmovie.dagger.module

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Module
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    internal fun providesApplication(): Application = application

    @Provides
    @Singleton
    internal fun provideContext(): Context = application.applicationContext
}