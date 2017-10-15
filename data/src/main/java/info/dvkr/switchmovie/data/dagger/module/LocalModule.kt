package info.dvkr.switchmovie.data.dagger.module

import android.content.Context
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import dagger.Module
import dagger.Provides
import info.dvkr.switchmovie.data.movie.repository.local.LocalRepository
import info.dvkr.switchmovie.data.movie.repository.local.LocalService
import javax.inject.Singleton


@Singleton
@Module
class LocalModule {
    @Singleton
    @Provides
    fun providePreferences(context: Context): Preferences = BinaryPreferencesBuilder(context)
            .registerPersistable(LocalService.LocalMovie.LOCAL_MOVIE_KEY, LocalService.LocalMovie::class.java)
            .registerPersistable(LocalService.LocalList.LOCAL_LIST_KEY, LocalService.LocalList::class.java)
            .build()

    @Singleton
    @Provides
    fun provideLocalRepository(preferences: Preferences) = LocalRepository(preferences)
}