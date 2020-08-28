package info.dvkr.switchmovie.data.repository.api

import com.elvishew.xlog.XLog
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create


class MovieApiService(
    private val ioScope: CoroutineScope, //TODO Can be multiple threads
    okHttpClient: OkHttpClient,
    apiBaseUrl: String,
    private val apiKey: String,
    private val apiBaseImageUrl: String
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val movieApi: MovieApi.Service =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(apiBaseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create()

    private var lastRequestedPage = 1
    private var isRequestInProgress = false

    fun clearLoadState() {
        XLog.d(getLog("clearLoadState"))
        lastRequestedPage = 1
        isRequestInProgress = false
    }

    suspend fun loadMoreMovies(): Either<Throwable, List<Movie>> {
        XLog.d(getLog("loadMoreMovies"))

        return withContext(ioScope.coroutineContext) {
            XLog.d(this@MovieApiService.getLog("loadMoreMovies", "Page: $lastRequestedPage"))

            Either<Throwable, List<Movie>> {
                isRequestInProgress.not() || throw IllegalStateException("Already loading page: $lastRequestedPage")
                isRequestInProgress = true

                try {
                    movieApi.getNowPlaying(apiKey, lastRequestedPage).items.map { it.toMovie(apiBaseImageUrl) }
                } finally {
                    isRequestInProgress = false
                }
            }
                .onSuccess { lastRequestedPage++ }
        }
    }
}