package info.dvkr.switchmovie.data.repository.api

import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Either
import info.dvkr.switchmovie.domain.utils.getTag
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.gildor.coroutines.retrofit.await
import timber.log.Timber


class MovieApiService(
    okHttpClient: OkHttpClient,
    apiBaseUrl: String,
    private val apiKey: String,
    private val apiBaseImageUrl: String
) {
    private val movieApi: MovieApi.Service

    init {
        val apiRetrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(apiBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        movieApi = apiRetrofit.create(MovieApi.Service::class.java)
    }

    private var lastRequestedPage = 1
    private var isRequestInProgress = false

    @Synchronized
    fun clearLoadState() {
        lastRequestedPage = 1
        isRequestInProgress = false
    }

    @Synchronized
    suspend fun loadMoreMovies(): Either<Throwable, List<Movie>> {
        Timber.tag(getTag("loadMoreMovies")).d("Invoked")

        val resultEither = requestMovies(lastRequestedPage)
        if (resultEither.isRight) lastRequestedPage++
        return resultEither
    }

    private suspend fun requestMovies(page: Int): Either<Throwable, List<Movie>> {
        Timber.tag(getTag("requestMovies")).d("Page: $page")

        isRequestInProgress.not() || return Either.Left(IllegalStateException("Already loading page: $page"))
        isRequestInProgress = true

        return try {
            movieApi.getNowPlaying(apiKey, page).await()
                .items
                .map { it.toMovie(apiBaseImageUrl) }
                .let { Either.Right(it) }
        } catch (ex: HttpException) {
            Either.Left(ex)
        } catch (ex: Throwable) {
            Either.Left(ex)
        } finally {
            isRequestInProgress = false
        }
    }
}