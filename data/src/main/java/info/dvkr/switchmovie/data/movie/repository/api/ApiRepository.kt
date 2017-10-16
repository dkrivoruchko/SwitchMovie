package info.dvkr.switchmovie.data.movie.repository.api

import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers


class ApiRepository(private val apiService: ApiService,
                    private val apiKey: String) {

    fun getMovies(page: Int): Observable<ApiService.ServerMovie> {
        Log.wtf("ApiRepository", "[${Thread.currentThread().name}] getMovies.page: $page")

        return apiService.getNowPlaying(apiKey, page)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .flatMap {
                    Log.wtf("ApiRepository", "[${Thread.currentThread().name}] apiService")
                    Observable.fromIterable(it.items)
                }
    }
}