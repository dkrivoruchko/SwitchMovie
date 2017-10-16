package info.dvkr.switchmovie.data.movie.repository.local

import android.util.Log
import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.domain.model.Movie
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class LocalRepository(private val preferences: Preferences) {

    fun putMovies(movieList: List<Movie>) {
        val localList = preferences.getPersistable(LocalService.LocalList.LOCAL_LIST_KEY, LocalService.LocalList())

        localList.items.addAll(movieList.map {
            LocalService.LocalMovie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage)
        })

        preferences.edit()
                .putPersistable(LocalService.LocalList.LOCAL_LIST_KEY, localList)
                .apply()
    }

    fun getMovies(): Observable<LocalService.LocalMovie> =
            Observable.just(preferences.getPersistable(LocalService.LocalList.LOCAL_LIST_KEY, LocalService.LocalList()))
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        Log.wtf("LocalRepository", "[${Thread.currentThread().name}] getMovies")
                        Observable.fromIterable(it.items)
                    }

}