package info.dvkr.switchmovie.domain.usecase

import info.dvkr.switchmovie.domain.helpers.Logger
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repositories.MovieRepository
import info.dvkr.switchmovie.domain.utils.Utils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.actor


class UseCasesImpl(
    private val movieRepository: MovieRepository.RW,
    private val logger: Logger
) : UseCases {

    private val sendChannel: SendChannel<UseCases.Request> = actor(CommonPool, Channel.UNLIMITED) {
        for (request in this) try {
            logger.LogD("UseCasesImpl: [${Utils.getLogPrefix(this)}] request: $request")

            when (request) {
                is UseCases.Request.GetMoviesFromIndex -> getMoviesFromIndex(request)
                is UseCases.Request.GetMovieById -> getMovieById(request)
                is UseCases.Request.StarMovieById -> starMovieById(request)
            }
        } catch (t: Throwable) {
            logger.LogE(t, "ExamUseCaseImpl:")
        }
    }

    override suspend fun send(request: UseCases.Request) = sendChannel.send(request)

    private suspend fun getMoviesFromIndex(request: UseCases.Request.GetMoviesFromIndex) {
        val moviesOnRange = try {
            movieRepository.getMoviesFromIndex(request.from)
        } catch (t: Throwable) {
            request.response.completeExceptionally(t)
            return
        }

        request.response.complete(moviesOnRange)
    }

    private suspend fun getMovieById(request: UseCases.Request.GetMovieById) {
        val movie = movieRepository.getMovieById(request.id)
        if (movie != null) request.response.complete(movie)
        else request.response.completeExceptionally(NoSuchElementException("No Movie found. Id = ${request.id}"))
    }

    private suspend fun starMovieById(request: UseCases.Request.StarMovieById) {
        val movie = movieRepository.getMovieById(request.id)
        if (movie == null)
            request.response.completeExceptionally(NoSuchElementException("No Movie found. Id = ${request.id}"))
        else {
            movie.let {
                Movie(
                    it.id, it.posterPath, it.title, it.overview,
                    it.releaseDate, it.voteAverage, !it.isStar
                )
            }
                .let { movieRepository.updateMovie(it) }

            request.response.complete(request.id)
        }
    }
}