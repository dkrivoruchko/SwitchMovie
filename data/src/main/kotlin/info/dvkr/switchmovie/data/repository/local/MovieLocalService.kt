package info.dvkr.switchmovie.data.repository.local

//import info.dvkr.switchmovie.data.notifications.NotificationManager
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Utils
import timber.log.Timber

class MovieLocalService(
    private val movieDao: MovieLocal.MovieDao
//        private val notificationManager: NotificationManager
//    preferences: Preferences
) {

    fun getMovies(): LiveData<List<Movie>> {
        Timber.d("[${Utils.getLogPrefix(this)}] getMovies")

        return Transformations.map(movieDao.getAll()) { list ->
            list.map { MovieLocal.MovieConverter.fromMovieDbToMovie(it) }
        }
    }

    fun getMovieById(movieId: Int): Movie? {
        Timber.d("[${Utils.getLogPrefix(this)}] getMovieById: $movieId")

        return movieDao.getMovieById(movieId)?.let { MovieLocal.MovieConverter.fromMovieDbToMovie(it) }
    }

    fun getMovieByIdLiveData(movieId: Int): LiveData<Movie> {
        Timber.d("[${Utils.getLogPrefix(this)}] getMovieByIdLiveData: $movieId")

        return Transformations.map(movieDao.getMovieByIdLiveData(movieId)) { movieDb ->
            MovieLocal.MovieConverter.fromMovieDbToMovie(movieDb)
        }
    }

    fun addMovies(inMovieList: List<Movie>) {
        Timber.d("[${Utils.getLogPrefix(this)}] addMovies: $inMovieList")

        movieDao.insertAll(inMovieList.map { MovieLocal.MovieConverter.fromMovieToMovieDb(it) })

        // TODO
//        inMovieList.forEach {
//            notificationManager.offerChangeEvent(
//                NotificationManager.ChangeEvent.OnMovieAdd(
//                    it
//                )
//            )
//        }
    }

    fun updateMovie(inMovie: Movie) {
        Timber.d("[${Utils.getLogPrefix(this)}] updateMovie: $inMovie")

        movieDao.insert(inMovie.let { MovieLocal.MovieConverter.fromMovieToMovieDb(it) })

//        notificationManager.offerChangeEvent(NotificationManager.ChangeEvent.OnMovieUpdate(inMovie)) TODO
    }

    fun deleteAll() {
        Timber.d("[${Utils.getLogPrefix(this)}] deleteAll")
        movieDao.deleteAll()
    }

}