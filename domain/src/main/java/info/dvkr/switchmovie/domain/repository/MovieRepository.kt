package info.dvkr.switchmovie.domain.repository

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.channels.SubscriptionReceiveChannel


interface MovieRepository {

    // From Presenters to MovieRepository
    @Keep sealed class Action {
        @Keep data class GetMoviesFromIndex(val from: Int) : Action()
        @Keep data class GetMovieById(val id: Int) : Action()
        @Keep data class StarMovieById(val id: Int) : Action()
    }

    // Events from Presenters to MovieRepository
    fun offer(action: MovieRepository.Action): Boolean

    // From MovieRepository to Presenters
    @Keep sealed class Result {
        @Keep data class MoviesOnRange(val range: Pair<Int, Int>, val moviesList: List<Movie>) : Result()
        @Keep data class MovieById(val movie: Movie) : Result()
        @Keep data class Error(val error: Throwable) : Result()
    }

    // Events from MovieRepository to Presenters
    fun subscribe(): SubscriptionReceiveChannel<Result>
}