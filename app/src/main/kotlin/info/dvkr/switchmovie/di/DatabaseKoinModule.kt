package info.dvkr.switchmovie.di

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import info.dvkr.switchmovie.data.repository.local.MovieLocal
import info.dvkr.switchmovie.data.repository.local.MovieLocalService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

@Database(entities = arrayOf(MovieLocal.MovieDb::class), version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieLocal.MovieDao
}

val databaseKoinModule = module {

    single {
        Room.databaseBuilder(
            androidContext().applicationContext,
            AppDatabase::class.java,
            "switchMovieDB"
        )
            .build()

    }

    single {
        MovieLocalService(
            get<AppDatabase>().movieDao()
//            BinaryPreferencesBuilder(androidContext())
//                .name("AppCache")
//                .registerPersistable(
//                    MovieLocal.LocalMovie.LOCAL_MOVIE_KEY, MovieLocal.LocalMovie::class.java
//                )
//                .registerPersistable(
//                    MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList::class.java
//                )
//                .exceptionHandler { Timber.e(it) }
//                .build()
        )
    }
}