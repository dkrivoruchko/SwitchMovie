package info.dvkr.switchmovie.domain.usecase

import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext


class UseCasesImpl(private val movieRepository: MovieRepository) : UseCases {

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> get(case: UseCases.Case<T>): T = withContext(CommonPool) {
        System.out.println("[${Utils.getLogPrefix(this)}] case: $case")

        when (case) {
            is UseCases.Case.GetMoviesFromIndex -> getMoviesFromIndex(case.from) as T
            is UseCases.Case.GetMovieById -> getMovieById(case.id) as T
            is UseCases.Case.StarMovieById -> starMovieById(case.id) as T
        }
    }

    @Throws(Throwable::class)
    private suspend fun getMoviesFromIndex(from: Int): UseCases.MoviesOnRange =
            movieRepository.get(MovieRepository.Request.GetMoviesFromIndex(from))
                    .run { UseCases.MoviesOnRange(this.range, this.moviesList) }

    @Throws(IllegalArgumentException::class)
    private suspend fun getMovieById(id: Int): Movie =
            movieRepository.get(MovieRepository.Request.GetMovieById(id))

    @Throws(IllegalArgumentException::class)
    private suspend fun starMovieById(id: Int): Int =
            movieRepository.get(MovieRepository.Request.GetMovieById(id))
                    .let {
                        Movie(it.id, it.posterPath, it.title, it.overview,
                                it.releaseDate, it.voteAverage, !it.isStar)
                    }
                    .let { movieRepository.get(MovieRepository.Request.UpdateMovie(it)) }
}