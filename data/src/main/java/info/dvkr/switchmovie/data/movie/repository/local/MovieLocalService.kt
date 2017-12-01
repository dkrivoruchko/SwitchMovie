package info.dvkr.switchmovie.data.movie.repository.local

import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.data.utils.bindPreference
import info.dvkr.switchmovie.domain.model.Movie
import timber.log.Timber

class MovieLocalService(private val preferences: Preferences) {

    private var localMovieList by bindPreference(preferences, MovieLocal.LocalList.LOCAL_LIST_KEY, MovieLocal.LocalList())

    fun putMovies(inMovieList: List<Movie>) {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] putMovies: $inMovieList")

        localMovieList.items.toMutableList()
                .apply {
                    addAll(inMovieList.map {
                        MovieLocal.LocalMovie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage)
                    })
                }
                .toList()
                .apply {
                    localMovieList = MovieLocal.LocalList(this)
                }
    }

    fun getMovies(): List<Movie> {
        Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovies")

        return localMovieList.items
                .onEach {
                    Timber.v("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] getMovies: $it")
                }
                .map { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage) }
    }
}