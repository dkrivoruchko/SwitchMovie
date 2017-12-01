package info.dvkr.switchmovie.data.movie.repository

import info.dvkr.switchmovie.data.movie.repository.api.MovieApiService
import info.dvkr.switchmovie.data.movie.repository.local.MovieLocalService
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.ThreadPoolDispatcher
import kotlinx.coroutines.experimental.channels.ActorJob
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.actor
import timber.log.Timber

class MovieRepositoryImpl(private val repositoryContext: ThreadPoolDispatcher,
                          private val movieApiService: MovieApiService,
                          private val movieLocalService: MovieLocalService) : MovieRepository {

    private val actorJob: ActorJob<MovieRepository.Action>
    private val broadcastChannel = ConflatedBroadcastChannel<MovieRepository.Result>()

    init {
        actorJob = actor(repositoryContext +
                CoroutineExceptionHandler { _, throwable ->
                    broadcastChannel.offer(MovieRepository.Result.Error(throwable))
                    Timber.e("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] error: $throwable")
                }, Channel.UNLIMITED) {
            for (action in this) {
                Timber.d("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] action: $action")

                when (action) {
                    is MovieRepository.Action.GetMoviesOnPage -> {
                        val itemsToSkip = (action.page - 1) * MovieRepository.MOVIES_PER_PAGE

                        // Checking for local data
                        movieLocalService.getMovies().asSequence()
                                .drop(itemsToSkip)
                                .take(MovieRepository.MOVIES_PER_PAGE)
                                .toList()
                                .let {
                                    if (it.isNotEmpty()) { // Have Local data
                                        return@let it
                                    } else { // No Local data.
                                        movieApiService.getMovies(action.page)
                                                .also { movieLocalService.putMovies(it) }
                                    }
                                }
                                .apply {
                                    if (itemsToSkip == 0) {
                                        broadcastChannel.send(MovieRepository.Result.Movies(this))
                                    } else {
                                        broadcastChannel.send(MovieRepository.Result.MoviesOnPage(this))
                                    }
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
                }

            }
        }

    }

    override fun offer(action: MovieRepository.Action): Boolean = actorJob.offer(action)

    override fun subscribe() = broadcastChannel.openSubscription()

}