package info.dvkr.switchmovie.data.repository.api

import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import ru.gildor.coroutines.retrofit.await


class MovieApiService(
    okHttpClient: OkHttpClient,
    apiBaseUrl: String,
    private val apiKey: String,
    private val apiBaseImageUrl: String
) {
    private val movieApi: MovieApi.Service =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(apiBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create()

    private var lastRequestedPage = 1
    private var isRequestInProgress = false

    @Synchronized
    fun clearLoadState() {
        lastRequestedPage = 1
        isRequestInProgress = false
    }

    @Synchronized
    suspend fun loadMoreMovies(): Either<Throwable, List<Movie>> {
        XLog.d(getLog("loadMoreMovies", "Invoked"))

        return requestMovies(lastRequestedPage).onSuccess { lastRequestedPage++ }
    }

    private suspend fun requestMovies(page: Int): Either<Throwable, List<Movie>> =
        withContext(Dispatchers.IO) {
            XLog.d(getLog("requestMovies", "Page: $page"))

            Either<Throwable, List<Movie>> {
                isRequestInProgress.not() || throw IllegalStateException("Already loading page: $page")
                isRequestInProgress = true

                try {
                    movieApi.getNowPlaying(apiKey, page).await()
                        .items.map { it.toMovie(apiBaseImageUrl) }
                } finally {
                    isRequestInProgress = false
                }
            }

        }
}