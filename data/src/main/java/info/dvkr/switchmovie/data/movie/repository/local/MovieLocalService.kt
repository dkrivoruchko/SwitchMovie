package info.dvkr.switchmovie.data.movie.repository.local

import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.data.utils.bindPreference
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.run
import timber.log.Timber

class MovieLocalService(private val serviceContext: ThreadPoolDispatcher,
                        preferences: Preferences) {

    private var localMovieList by bindPreference(preferences, MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList())

    suspend fun addMovies(inMovieList: List<Movie>) = run(serviceContext) {

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

    suspend fun getMovies(): List<Movie> = run(serviceContext) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovies")

        return@run localMovieList.items
                .map { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, it.isStar) }
    }

    suspend fun updateMovie(inMovie: Movie): Int = run(serviceContext) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] updateMovie: $inMovie")

        val mutableList = localMovieList.items.toMutableList()
        var index = -1
        mutableList.asSequence()
                .onEach { index++ }
                .filter { it.id == inMovie.id }
                .firstOrNull()
                .apply { if (this == null) return@run -1 }

        val localMovie = MovieLocal.LocalMovie(inMovie.id, inMovie.posterPath, inMovie.title, inMovie.overview, inMovie.releaseDate, inMovie.voteAverage, inMovie.isStar)
        mutableList[index] = localMovie
        localMovieList = MovieLocal.LocalList(mutableList.toList())
        return@run index
    }

    suspend fun getMovieById(movieId: Int): Movie? = run(serviceContext) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovieById: $movieId")
        delay(5000)
        return@run localMovieList.items.asSequence()
                .filter { it.id == movieId }
                .firstOrNull()
                ?.let { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, it.isStar) }
    }
}