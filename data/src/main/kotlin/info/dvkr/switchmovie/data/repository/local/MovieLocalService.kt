package info.dvkr.switchmovie.data.repository.local

import android.arch.lifecycle.LiveData
import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.data.utils.bindPreference
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.getTag
import org.threeten.bp.LocalDate
import timber.log.Timber

class MovieLocalService(
    private val movieDao: MovieLocal.MovieDao,
    private val moviePreferences: Preferences
) {

    private var lastMovieUpdateDate: Long by bindPreference(
        moviePreferences, "KEY_LAST_MOVIE_UPDATE_DATE", LocalDate.now().minusDays(2).toEpochDay()
    )


    fun getMovies(): LiveData<List<Movie>> {
        Timber.tag(getTag("getMovies")).d("Invoked")
        return movieDao.getAll()
    }

    fun getMovieById(movieId: Int): Movie? {
        Timber.tag(getTag("getMovieById")).d(movieId.toString())
        return movieDao.getMovieById(movieId)
    }

    fun getMovieByIdLiveData(movieId: Int): LiveData<Movie> {
        Timber.tag(getTag("getMovieByIdLiveData")).d(movieId.toString())

        return movieDao.getMovieByIdLiveData(movieId)
    }

    fun getLastMovieUpdateDate(): LocalDate {
        Timber.tag(getTag("getLastMovieUpdateDate")).d("Invoked")

        return LocalDate.ofEpochDay(lastMovieUpdateDate)
    }

    fun addMovies(inMovieList: List<Movie>) {
        Timber.tag(getTag("addMovies")).d(inMovieList.toString())

        movieDao.insertAll(inMovieList.map { MovieLocal.MovieConverter.fromMovieToMovieDb(it) })
    }

    fun updateMovie(inMovie: Movie) {
        Timber.tag(getTag("updateMovie")).d(inMovie.toString())

        movieDao.insert(inMovie.let { MovieLocal.MovieConverter.fromMovieToMovieDb(it) })
    }

    fun deleteAll() {
        Timber.tag(getTag("deleteAll")).d("Invoked")

        movieDao.deleteAll()
    }

    fun setLastMovieUpdateDate(localDate: LocalDate) {
        Timber.tag(getTag("setLastMovieUpdateDate")).d(localDate.toString())

        lastMovieUpdateDate = localDate.toEpochDay()
    }
}