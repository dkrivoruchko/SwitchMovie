package info.dvkr.switchmovie.data.movie.repository.api

import android.util.Log
import io.reactivex.schedulers.Schedulers


class ApiRepository(private val apiService: ApiService,
                    private val apiKey: String) {

    fun getMovies(page: Int): List<ApiService.ServerMovie> {
        Log.wtf("ApiRepository", "[${Thread.currentThread().name}] getMovies.page: $page")

        return apiService.getNowPlaying(apiKey, page)
                .subscribeOn(Schedulers.io())
                .map {
                    Log.wtf("ApiRepository", "[${Thread.currentThread().name}] MAP")
                    it.items
                }
                .blockingGet()

    }
}
