package info.dvkr.switchmovie.data.movie.repository

import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.run
import timber.log.Timber

class MovieRepositoryImpl(private val movieApiService: MovieApiService,
                          private val movieLocalService: MovieLocalService) : MovieRepository {

  @Suppress("UNCHECKED_CAST")
  override suspend fun <T> get(request: MovieRepository.Request<T>): T = run(CommonPool) {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] request: $request")
    when (request) {
      is MovieRepository.Request.GetMoviesFromIndex -> getMoviesFromIndex(request.from) as T
      is MovieRepository.Request.GetMovieById -> getMovieById(request.id) as T
      is MovieRepository.Request.UpdateMovie -> updateMovie(request.movie) as T
    }
  }

  @Throws(Throwable::class)
  private suspend fun getMoviesFromIndex(from: Int): MovieRepository.MoviesOnRange {
    val pages: Int = from / MovieApi.MOVIES_PER_PAGE
    // Checking for local data
    movieLocalService.getMovies()
        .asSequence()
        .drop(pages * MovieApi.MOVIES_PER_PAGE)
        .take(MovieApi.MOVIES_PER_PAGE)
        .toList()
        .run {
          // Have Local data
          if (this.isNotEmpty()) this
          // No Local data.
          else movieApiService.getMovies(pages + 1).also { movieLocalService.addMovies(it) }
        }
        .run {
          val from = pages * MovieApi.MOVIES_PER_PAGE
          val range = Pair(from, from + this.size)
          return MovieRepository.MoviesOnRange(range, this)
        }
  }

  @Throws(IllegalArgumentException::class)
  private suspend fun getMovieById(id: Int): Movie =
      movieLocalService.getMovieById(id)
          .run {
            if (this == null) throw IllegalArgumentException("Movie not found")
            this
          }

  @Throws(IllegalArgumentException::class)
  private suspend fun updateMovie(movie: Movie): Int =
      movieLocalService.updateMovie(movie)
          .run {
            delay(10000)
            if (this < 0) throw IllegalArgumentException("Updating movie. Movie not found")
            this
          }
}