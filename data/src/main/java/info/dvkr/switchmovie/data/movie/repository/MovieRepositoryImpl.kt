package info.dvkr.switchmovie.data.movie.repository

import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import timber.log.Timber

class MovieRepositoryImpl(private val movieApiService: MovieApiService,
                          private val movieLocalService: MovieLocalService) : MovieRepository {

    private val actorJob: ActorJob<MovieRepository.Request>
    private val movieMutex = Mutex()

    init {
        actorJob = actor(CommonPool, Channel.UNLIMITED) {
            for (request in channel) {
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] request: $request")

                when (request) {
                    is MovieRepository.Request.GetMoviesFromIndex -> async(coroutineContext) {
                        movieMutex.withLock {
                            val pages: Int = request.from / MovieApi.MOVIES_PER_PAGE

                            // Checking for local data
                            movieLocalService.getMovies().asSequence()
                                    .drop(pages * MovieApi.MOVIES_PER_PAGE)
                                    .take(MovieApi.MOVIES_PER_PAGE)
                                    .toList()
                                    .run {
                                        if (this.isNotEmpty()) this // Have Local data
                                        else  // No Local data.
                                            try {
                                                movieApiService.getMovies(pages + 1)
                                                        .also { movieLocalService.addMovies(it) }
                                            } catch (t: Throwable) {
                                                request.response.completeExceptionally(t)
                                                Timber.e(t)
                                                return@async
                                            }
                                    }
                                    .apply {
                                        val from = pages * MovieApi.MOVIES_PER_PAGE
                                        val range = Pair(from, from + this.size)
                                        request.response.complete(MovieRepository.MoviesOnRange(range, this))
                                    }
                        }
                    }

                    is MovieRepository.Request.GetMovieById -> async(coroutineContext) {
                        movieMutex.withLock {
                            movieLocalService.getMovies().asSequence()
                                    .filter { it.id == request.id }
                                    .firstOrNull()
                                    .apply {
                                        if (this == null) request.response.completeExceptionally(IllegalArgumentException("Movie not found"))
                                        else request.response.complete(this)
                                    }
                        }
                    }

                    is MovieRepository.Request.StarMovieById -> async(coroutineContext) {
                        movieMutex.withLock {
                            movieLocalService.getMovieById(request.id)
                                    .run {
                                        if (this@run == null) {
                                            request.response.completeExceptionally(IllegalArgumentException("Movie not found"))
                                            return@withLock
                                        } else this
                                    }
                                    .let { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, !it.isStar) }
                                    .let { movieLocalService.updateMovie(it) }
                                    .apply {
                                        if (this < 0) request.response.completeExceptionally(IllegalArgumentException("Updating movieData. Movie not found"))
                                        else request.response.complete(this)
                                    }
                        }
                    }
                }
            }
        }
    }

    override suspend fun send(request: MovieRepository.Request) = actorJob.send(request)
}