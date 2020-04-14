package info.dvkr.switchmovie.data.repository.local

import com.elvishew.xlog.XLog
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.utils.getLog
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

class MovieLocalService(
    private val movieDao: MovieLocal.MovieDao,
    private val settings: Settings
) {

    fun getMovies(): Flow<List<Movie>> {
        XLog.d(getLog("getMovies"))
        return movieDao.getAll()
    }

    suspend fun getMovieById(movieId: Int): Movie? {
        XLog.d(getLog("getMovieById", "$movieId"))
        return movieDao.getMovieById(movieId)
    }

    fun getMovieFlowById(movieId: Int): Flow<Movie> {
        XLog.d(getLog("getMovieFlowById", "$movieId"))
        return movieDao.getMovieFlowById(movieId)
    }

    fun getLastMovieUpdateDate(): LocalDate {
        XLog.d(getLog("getLastMovieUpdateDate"))
        return LocalDate.ofEpochDay(settings.lastMovieUpdateDate)
    }

    suspend fun addMovies(inMovieList: List<Movie>) {
        XLog.d(getLog("addMovies", inMovieList.joinToString()))
        movieDao.insertAll(inMovieList.map { MovieLocal.MovieConverter.fromMovieToMovieDb(it) })
    }

    suspend fun updateMovie(inMovie: Movie) {
        XLog.d(getLog("updateMovie", "$inMovie"))
        movieDao.insert(inMovie.let { MovieLocal.MovieConverter.fromMovieToMovieDb(it) })
    }

    suspend fun deleteAll() {
        XLog.d(getLog("deleteAll"))
        movieDao.deleteAll()
    }

    fun setLastMovieUpdateDate(localDate: LocalDate) {
        XLog.d(getLog("setLastMovieUpdateDate", "$localDate"))
        settings.lastMovieUpdateDate = localDate.toEpochDay()
    }
}