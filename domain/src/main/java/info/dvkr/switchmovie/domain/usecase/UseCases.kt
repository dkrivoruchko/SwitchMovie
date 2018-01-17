package info.dvkr.switchmovie.domain.usecase

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie


interface UseCases {
    @Keep data class MoviesOnRange(val range: Pair<Int, Int>, val moviesList: List<Movie>)

    // From Presenters to MovieRepository
    @Keep sealed class Case<T> {
        @Keep data class GetMoviesFromIndex(val from: Int) : Case<MoviesOnRange>()
        @Keep data class GetMovieById(val id: Int) : Case<Movie>()
        @Keep data class StarMovieById(val id: Int) : Case<Int>()
    }

    // Events from Presenters to MovieRepository
    @Throws(Throwable::class)
    suspend fun <T> get(case: UseCases.Case<T>): T
}