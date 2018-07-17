package info.dvkr.switchmovie.data.repository.api

import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.base.Result
import info.dvkr.switchmovie.domain.usecase.base.BaseUseCaseException
import info.dvkr.switchmovie.domain.utils.Utils
import okhttp3.OkHttpClient
import retrofit2.Call
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

    /**
     * Suspend extension that allows suspend [Call] inside coroutine.
     */
//    suspend fun <T : Any> Call<T>.awaitCallResult(): Either<Throwable, T> {
//        return suspendCancellableCoroutine { continuation ->
//            Log.e(">>>>>>", "suspendCancellableCoroutine: ${Thread.currentThread().name}")
////            try {
////                val response: Response<T> = execute()
////                Log.e(">>>>>>", "execute.onResponse: ${Thread.currentThread().name}")
////                continuation.resume(
////                    if (response.isSuccessful) {
////                        val body = response.body()
////                        if (body == null) {
////                            Either.Left(NullPointerException("Response body is null"))
////                        } else {
////                            Either.Right(body)
////                        }
////                    } else {
////                        Either.Left(HttpException(response))
////                    }
////                )
////            } catch (ex: IOException) {
////                Log.e(">>>>>>", "enqueue.onFailure: ${Thread.currentThread().name}")
////                if (continuation.isCancelled.not()) continuation.resume(Either.Left(ex))
////            }
//
//            enqueue(object : Callback<T> {
//                @MainThread
//                override fun onResponse(call: Call<T>?, response: Response<T>) {
////                    launch(continuation.context) {
//                    Log.e(">>>>>>", "enqueue.onResponse: ${Thread.currentThread().name}")
//                    continuation.resume(
//                        if (response.isSuccessful) {
//                            val body = response.body()
//                            if (body == null) {
//                                Either.Left(NullPointerException("Response body is null"))
//                            } else {
//                                Either.Right(body)
//                            }
//                        } else {
//                            Either.Left(HttpException(response))
//                        }
//                    )
////                    }
//                }
//
//
//                @MainThread
//                override fun onFailure(call: Call<T>, error: Throwable) {
//                    // Don'error bother with resuming the continuation if it is already cancelled.
//                    Log.e(">>>>>>", "enqueue.onFailure: ${Thread.currentThread().name}")
//                    if (continuation.isCancelled) return
//                    continuation.resume(Either.Left(error))
//                }
//            })
//
//            continuation.invokeOnCancellation {
//                Log.e(">>>>>>", " continuation.invokeOnCancellation: ${Thread.currentThread().name}")
//                if (continuation.isCancelled)
//                    try {
//                        cancel()
//                    } catch (ignore: Throwable) {
//                        //Ignore cancel exception
//                    }
//            }
//        }
//    }

    private var lastRequestedPage = 1
    private var isRequestInProgress = false

    fun clearLoadState() {
        lastRequestedPage = 1
        isRequestInProgress = false
        // TODO cansel loading is running
    }

    suspend fun loadMoreMovies(): Result<List<Movie>> {
        Timber.e("[${Utils.getLogPrefix(this)}] loadMoreMovies")

        val result = requestMovies(lastRequestedPage)

        when (result) {
            is Result.Success -> lastRequestedPage++
            is Result.Error -> Unit
            is Result.InProgress -> Unit
        }

        return result
    }

    // TODO Add cancel option
    // TODO Check call thread
    // TODO Define exact exceptions
    // TODO checl multithreding
//    fun requestMovies(page: Int): Result<List<Movie>> {
//        Timber.e("[${Utils.getLogPrefix(this)}] loadMoreMovies.page: $page")
//
//        isRequestInProgress.not() || return Result.Error(IllegalStateException("Already loading"))
//        isRequestInProgress = true
//
//        try {
//            val response = movieApi.getNowPlaying(apiKey, page).execute()
//
//            response.isSuccessful || return Result.Error(UseCaseException.NetworkException(HttpException(response)))
//
//            return response.body()?.items?.map { it.toMovie(apiBaseImageUrl) }?.let { Result.Success(it) }
//                ?: Result.Error(UseCaseException.NetworkException(IllegalStateException("Empty result")))
//
//        } catch (ex: IOException) {
//            return Result.Error(UseCaseException.NetworkException(ex))
//        } catch (ex: RuntimeException) {
//            return Result.Error(UseCaseException.NetworkException(ex))
//        }
//    }

    // TODO Add cancel option
    // TODO Check call thread
    // TODO Define exact exceptions
    // TODO checl multithreding
    suspend fun requestMovies(page: Int): Result<List<Movie>> {
        Timber.e("[${Utils.getLogPrefix(this)}] requestMovies.page: $page")

        isRequestInProgress.not() || return Result.Error(IllegalStateException("Already loading"))
        isRequestInProgress = true

        return try {
            movieApi.getNowPlaying(apiKey, page).await()
                .items
                .map { it.toMovie(apiBaseImageUrl) }
                .let { Result.Success(it) }
        } catch (ex: HttpException) {
            Result.Error(BaseUseCaseException.NetworkException(ex))
        } catch (ex: Throwable) {
            Result.Error(BaseUseCaseException.NetworkException(ex))
        } finally {
            isRequestInProgress = false
        }
    }

//    fun loadMoreMovies(page: Int, success: (List<Movie>) -> Unit, failure: (Throwable) -> Unit) {
//        Timber.d("[${Utils.getLogPrefix(this)}] loadMoreMovies.page: $page")
//
//
//        movieApi.getNowPlaying(apiKey, page).enqueue(
//            object : Callback<MovieApi.MovieListRaw> {
//                @MainThread
//                override fun onResponse(call: Call<MovieApi.MovieListRaw>?, response: Response<MovieApi.MovieListRaw>) {
//                    Log.e(">>>>>>", "enqueue.onResponse: ${Thread.currentThread().name}")
//
//                    if (response.isSuccessful) {
//                        val body = response.body()
//                        if (body == null) {
//                            failure.invoke(NullPointerException("Response body is null"))
//                        } else {
//                            success.invoke(body.items.map { it.toMovie(apiBaseImageUrl) })
//                        }
//                    } else {
//                        failure.invoke(HttpException(response))
//                    }
//                }
//
//
//                @MainThread
//                override fun onFailure(call: Call<MovieApi.MovieListRaw>, error: Throwable) {
//                    // Don'error bother with resuming the continuation if it is already cancelled.
//                    Log.e(">>>>>>", "enqueue.onFailure: ${Thread.currentThread().name}")
//                    failure.invoke(error)
//                }
//            }
//        )
//
//    }
}