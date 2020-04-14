package info.dvkr.switchmovie.domain.repositories

import androidx.lifecycle.LiveData
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.utils.Either
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate


interface MovieRepository {
    interface RO {

        fun getMovies(): Flow<List<Movie>>

        suspend fun getMovieById(movieId: Int): Movie?

        fun getMovieFlowById(movieId: Int): Flow<Movie>

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