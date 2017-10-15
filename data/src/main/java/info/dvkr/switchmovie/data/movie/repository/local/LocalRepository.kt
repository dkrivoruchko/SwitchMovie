package info.dvkr.switchmovie.data.movie.repository.local

import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.domain.model.Movie

class LocalRepository(private val preferences: Preferences) {

    fun putMovies(movieList: List<Movie>) {
        val localList = getMovies()

        localList.items.addAll(movieList.map {
            LocalService.LocalMovie(it.id, it.posterPath, it.originalTitle, it.overview, it.releaseDate, it.voteAverage)
        })

        preferences.edit()
                .putPersistable(LocalService.LocalList.LOCAL_LIST_KEY, localList)
                .apply()
    }

    fun getMovies() =
            preferences.getPersistable(LocalService.LocalList.LOCAL_LIST_KEY, LocalService.LocalList())

}