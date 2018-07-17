package info.dvkr.switchmovie.data.movie.repository

import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.usecase.UseCases

class MovieRepositoryImpl(
    private val movieApiService: MovieApiService,
    private val movieLocalService: MovieLocalService
) : MovieRepository.RW {

    @Throws(Throwable::class)
    override suspend fun getMoviesFromIndex(from: Int): UseCases.MoviesOnRange {
        val pages: Int = from / MovieApi.MOVIES_PER_PAGE
        // Checking for local data
        movieLocalService.getMovies().asSequence()
            .drop(pages * MovieApi.MOVIES_PER_PAGE)
            .take(MovieApi.MOVIES_PER_PAGE)
            .toList()
            .run {
                // Have Local data
                if (this.isNotEmpty()) this
                // No Local data.
                else movieApiService.getMovies(pages + 1).also { movieLocalService.addMovies(it) }
            }
            .run {
                val from = pages * MovieApi.MOVIES_PER_PAGE
                val range = Pair(from, from + this.size)
                return UseCases.MoviesOnRange(range, this)
            }
    }

    override suspend fun getMovieById(id: Int): Movie? = movieLocalService.getMovieById(id)

    @Throws(IllegalArgumentException::class)
    override suspend fun updateMovie(movie: Movie): Int = movieLocalService.updateMovie(movie)
        .run {
            if (this < 0) throw IllegalArgumentException("Updating movie. Movie not found")
            this
        }
}