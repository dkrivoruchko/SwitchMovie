package info.dvkr.switchmovie.data.movie.repository.local

import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.data.utils.bindPreference
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import timber.log.Timber

class MovieLocalService(preferences: Preferences) {

  private var localMovieList by bindPreference(preferences, MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList())
  private val movieMutex = Mutex()

  suspend fun getMovies(): List<Movie> = movieMutex.withLock {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovies")

    return localMovieList.items
        .map { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, it.isStar) }
  }

  suspend fun getMovieById(movieId: Int): Movie? = movieMutex.withLock {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovieById: $movieId")

    return localMovieList.items.asSequence()
        .filter { it.id == movieId }
        .firstOrNull()
        ?.let { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, it.isStar) }
  }

  suspend fun addMovies(inMovieList: List<Movie>) = movieMutex.withLock {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] addMovies: $inMovieList")

    localMovieList.items.toMutableList()
        .apply {
          addAll(inMovieList.map {
            MovieLocal.LocalMovie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, it.isStar)
          })
        }
        .toList()
        .apply {
          localMovieList = MovieLocal.LocalList(this)
        }
  }

  suspend fun updateMovie(inMovie: Movie): Int = movieMutex.withLock {
    Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] updateMovie: $inMovie")

    var index = -1
    val mutableList = localMovieList.items.toMutableList()
    mutableList.asSequence()
        .onEach { index++ }
        .filter { it.id == inMovie.id }
        .firstOrNull()
        .apply { if (this == null) return -1 }

    val localMovie = MovieLocal.LocalMovie(inMovie.id, inMovie.posterPath, inMovie.title, inMovie.overview, inMovie.releaseDate, inMovie.voteAverage, inMovie.isStar)
    mutableList[index] = localMovie
    localMovieList = MovieLocal.LocalList(mutableList.toList())
    return index
  }
}