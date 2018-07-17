package info.dvkr.switchmovie.domain.repositories

import android.arch.lifecycle.LiveData
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.base.Result


interface MovieRepository {
    interface RO {

        fun clearLoadState(): Unit

        suspend fun loadMoreMovies(): Result<List<Movie>>

        fun getMovies(): LiveData<List<Movie>>

        fun getMovieById(movieId: Int): Movie?

        fun getMovieByIdLiveData(movieId: Int): LiveData<Movie>
    }

    interface RW : RO {
        fun addMovies(inMovieList: List<Movie>)

        fun updateMovie(movie: Movie)

        fun deleteMovies()
    }
}