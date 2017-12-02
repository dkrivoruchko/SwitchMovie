package info.dvkr.switchmovie.data.movie.repository

import info.dvkr.switchmovie.data.movie.repository.api.MovieApi
import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

class MovieRepositoryImpl(repositoryContext: ThreadPoolDispatcher,
                          private val movieApiService: MovieApiService,
                          private val movieLocalService: MovieLocalService) : MovieRepository {

    private val actorJob: ActorJob<MovieRepository.Action>
    private val broadcastChannel = ConflatedBroadcastChannel<MovieRepository.Result>()

    init {
        actorJob = actor(repositoryContext +
                CoroutineExceptionHandler { _, throwable ->
                    // TODO
                    broadcastChannel.offer(MovieRepository.Result.Error(throwable))
                    Timber.e("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] error: $throwable")
                }, Channel.UNLIMITED) {
            for (action in this) {
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] action: $action")

                when (action) {
                    is MovieRepository.Action.GetMoviesFromIndex -> {
                        val pages: Int = action.from / MovieApi.MOVIES_PER_PAGE

                        // Checking for local data
                        movieLocalService.getMovies().asSequence()
                                .drop(pages * MovieApi.MOVIES_PER_PAGE)
                                .take(MovieApi.MOVIES_PER_PAGE)
                                .toList()
                                .let {
                                    if (it.isNotEmpty()) { // Have Local data
                                        return@let it
                                    } else { // No Local data.
                                        movieApiService.getMovies(pages + 1)
                                                .also { movieLocalService.addMovies(it) }
                                    }
                                }
                                .apply {
                                    val from = pages * MovieApi.MOVIES_PER_PAGE
                                    val range = Pair(from, from + this.size)
                                    broadcastChannel.send(MovieRepository.Result.MoviesOnRange(range, this))
                                }
                    }

                    is MovieRepository.Action.GetMovieById -> {
                        // Searching for local repository
                        movieLocalService.getMovies().asSequence()
                                .filter { it.id == action.id }
                                .first()
                                .let { broadcastChannel.send(MovieRepository.Result.MovieById(it)) }
//  { error -> broadcastChannel.send(MovieRepository.Result.Error(IllegalStateException("Movie not found. ID: ${action.id}"))) }
                    }

                    is MovieRepository.Action.StarMovieById -> {
                        movieLocalService.getMovieById(action.id)
                                ?.let { Movie(it.id, it.posterPath, it.title, it.overview, it.releaseDate, it.voteAverage, !it.isStar) }
                                ?.let { movieLocalService.updateMovie(it) }
                                ?.apply { offer(MovieRepository.Action.GetMoviesFromIndex(this)) }
                    }
                }

            }
        }

    }

    override fun offer(action: MovieRepository.Action): Boolean = actorJob.offer(action)

    override fun subscribe() = broadcastChannel.openSubscription()

}