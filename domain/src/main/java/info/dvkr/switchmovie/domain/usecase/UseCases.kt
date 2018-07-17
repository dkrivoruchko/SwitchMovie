package info.dvkr.switchmovie.domain.usecase

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.CompletableDeferred


interface UseCases {
    @Keep data class MoviesOnRange(val range: Pair<Int, Int>, val moviesList: List<Movie>)

    @Keep sealed class Request {
        @Keep data class GetMoviesFromIndex(
            val from: Int, val response: CompletableDeferred<MoviesOnRange>
        ) : Request()

        @Keep data class GetMovieById(
            val id: Int, val response: CompletableDeferred<Movie>
        ) : Request()

        @Keep data class StarMovieById(
            val id: Int, val response: CompletableDeferred<Int>
        ) : Request()
    }

    suspend fun send(request: UseCases.Request)
}