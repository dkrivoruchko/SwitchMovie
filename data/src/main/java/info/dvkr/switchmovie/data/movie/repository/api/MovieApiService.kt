package info.dvkr.switchmovie.data.movie.repository.api

import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.run
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class MovieApiService(private val movieApi: MovieApi.Service,
                      private val apiKey: String) {

  suspend fun getMovies(page: Int): List<Movie> = run(CommonPool) {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovies.page: $page")

    return@run getNowPlayingMovies(page).items
        .map {
          Movie(it.id,
              BuildConfig.BASE_IMAGE_URL + it.posterPath,
              it.title,
              it.overview,
              it.releaseDate,
              it.voteAverage,
              false)
        }
  }

  private suspend fun getNowPlayingMovies(page: Int): MovieApi.ServerList =
      movieApi.getNowPlaying(apiKey, page).await()


  private suspend fun <T> Call<T>.await(): T = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback<T> {
      override fun onResponse(call: Call<T>, response: Response<T>) {
        Timber.v("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] onResponse: $response")

        continuation.isActive || return
        try {
          response.isSuccessful || throw IllegalStateException("Http error ${response.code()}")
          continuation.resume(response.body() ?: throw IllegalStateException("Response body is null"))
        } catch (t: Throwable) {
          continuation.resumeWithException(t)
        }
      }

      override fun onFailure(call: Call<T>, t: Throwable) {
        Timber.v("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] onFailure: $t")

        continuation.isActive || return
        continuation.resumeWithException(t)
      }
    })

    continuation.invokeOnCompletion {
      if (continuation.isCancelled) cancel()
    }
  }
}