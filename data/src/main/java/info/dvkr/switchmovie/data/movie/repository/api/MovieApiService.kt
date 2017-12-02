package info.dvkr.switchmovie.data.movie.repository.api

import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.run
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


class MovieApiService(private val serviceContext: ThreadPoolDispatcher,
                      private val movieApi: MovieApi.Service,
                      private val apiKey: String) {

    suspend fun getMovies(page: Int): List<Movie> = run(serviceContext) {
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
        val callback = object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) = continuation.tryToResume {
                response.isSuccessful || throw IllegalStateException("Http error ${response.code()}")

                response.body() ?: throw IllegalStateException("Response body is null")
            }

            override fun onFailure(call: Call<T>, t: Throwable) = continuation.tryToResume { throw t }
        }

        enqueue(callback)

        continuation.invokeOnCompletion {
            if (continuation.isCancelled) cancel()
            this@await.cancel()
        }
    }

    private inline fun <T> CancellableContinuation<T>.tryToResume(getter: () -> T) {
        isActive || return
        try {
            resume(getter())
        } catch (exception: Throwable) {
            resumeWithException(exception)
        }
    }
}