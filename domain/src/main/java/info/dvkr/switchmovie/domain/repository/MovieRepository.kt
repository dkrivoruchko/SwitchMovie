package info.dvkr.switchmovie.domain.repository

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie


interface MovieRepository {
  @Keep data class MoviesOnRange(val range: Pair<Int, Int>, val moviesList: List<Movie>)

  // From Presenters to MovieRepository
  @Keep sealed class Request<T> {
    @Keep data class GetMoviesFromIndex(val from: Int) : Request<MoviesOnRange>()
    @Keep data class GetMovieById(val id: Int) : Request<Movie>()
    @Keep data class StarMovieById(val id: Int) : Request<Int>()
  }

  // Events from Presenters to MovieRepository
  @Throws(Throwable::class)
  suspend fun <T> get(request: MovieRepository.Request<T>): T
}