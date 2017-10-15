package info.dvkr.switchmovie.domain.repository

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import io.reactivex.Observable


interface MovieRepository {

    companion object {
        const val MOVIES_PER_PAGE = 20
    }

    // From Presenters to MovieRepository
    @Keep sealed class Action {
        @Keep data class GetMoviesOnPage(val page: Int) : Action()
        @Keep data class GetMovieById(val id: Int) : Action()
    }

    // Events from Presenters to MovieRepository
    fun actions(action: Action)

    // From MovieRepository to Presenters
    @Keep sealed class Result {
        @Keep data class Movies(val moviesList: List<Movie>) : Result()
        @Keep data class MoviesOnPage(val moviesList: List<Movie>) : Result()
        @Keep data class MovieById(val movie: Movie) : Result()
        @Keep data class Error(val error: Throwable) : Result()
    }

    // Events from MovieRepository to Presenters
    fun results(): Observable<Result>
}