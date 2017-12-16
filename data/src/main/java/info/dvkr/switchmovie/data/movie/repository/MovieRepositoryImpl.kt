package info.dvkr.switchmovie.data.movie.repository

import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.run

class MovieRepositoryImpl(private val repositoryContext: ThreadPoolDispatcher,
                          private val movieApiService: MovieApiService,
                          private val movieLocalService: MovieLocalService) : MovieRepository {

  @Suppress("UNCHECKED_CAST")
  override suspend fun <T> get(request: MovieRepository.Request<T>): T = run(repositoryContext) {
    when (request) {
      is MovieRepository.Request.GetMoviesFromIndex -> getMoviesFromIndex(request) as T
      is MovieRepository.Request.GetMovieById -> getMovieById(request) as T
      is MovieRepository.Request.StarMovieById -> starMovieById(request) as T
    }
  }

  @Throws(Throwable::class)
  private suspend fun getMoviesFromIndex(request: MovieRepository.Request.GetMoviesFromIndex)
      : MovieRepository.MoviesOnRange {

    val pages: Int = request.from / MovieApi.MOVIES_PER_PAGE
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
  private suspend fun getMovieById(request: MovieRepository.Request.GetMovieById): Movie =
      movieLocalService.getMovies()
          .asSequence()
          .filter { it.id == request.id }
          .firstOrNull()
          .run {
            if (this == null) throw IllegalArgumentException("Movie not found")
            this
          }

  @Throws(IllegalArgumentException::class)
  private suspend fun starMovieById(request: MovieRepository.Request.StarMovieById): Int =
      movieLocalService.getMovieById(request.id)
          .run {
            delay(5000)
            if (this@run == null) throw IllegalArgumentException("Movie not found")
            else this
          }
          .let { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, !it.isStar) }
          .let { movieLocalService.updateMovie(it) }
          .run {
            if (this < 0) throw IllegalArgumentException("Updating movie. Movie not found")
            this
          }
}