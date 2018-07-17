package info.dvkr.switchmovie.domain.repositories

import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.usecase.UseCases


interface MovieRepository {
    interface RO {
        suspend fun getMoviesFromIndex(from: Int): UseCases.MoviesOnRange
        suspend fun getMovieById(id: Int): Movie?
    }

    interface RW : RO {
        suspend fun updateMovie(movie: Movie): Int
    }
}