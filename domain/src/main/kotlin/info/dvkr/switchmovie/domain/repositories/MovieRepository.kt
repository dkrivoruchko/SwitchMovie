package info.dvkr.switchmovie.domain.repositories

import androidx.lifecycle.LiveData
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Either
import org.threeten.bp.LocalDate


interface MovieRepository {
    interface RO {

        fun getMovies(): LiveData<List<Movie>>

        suspend fun getMovieById(movieId: Int): Movie?

        fun getMovieByIdLiveData(movieId: Int): LiveData<Movie>

        fun getLastMovieUpdateDate(): LocalDate
    }

    interface RW : RO {

        fun clearLoadState()

        suspend fun loadMoreMovies(): Either<Throwable, List<Movie>>

        suspend fun addMovies(inMovieList: List<Movie>)

        suspend fun updateMovie(movie: Movie)

        suspend fun deleteMovies()

        fun setLastMovieUpdateDate(localDate: LocalDate)
    }
}