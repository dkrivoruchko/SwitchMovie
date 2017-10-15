package info.dvkr.switchmovie.data.movie.repository

import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import info.dvkr.switchmovie.data.movie.repository.api.ApiRepository
import info.dvkr.switchmovie.data.movie.repository.local.LocalRepository
import info.dvkr.switchmovie.domain.BuildConfig
import info.dvkr.switchmovie.domain.model.Movie
import info.dvkr.switchmovie.domain.repository.MovieRepository
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class MovieRepositoryImpl(private val apiRepository: ApiRepository,
                          private val localRepository: LocalRepository) : MovieRepository {

    private val results = PublishRelay.create<MovieRepository.Result>()

    override fun actions(action: MovieRepository.Action) {
        Single.just(action).subscribeOn(Schedulers.single()).subscribe { action ->
            Log.wtf("MovieRepositoryImpl", "[${Thread.currentThread().name}] action: $action")

            when (action) {
                is MovieRepository.Action.GetMoviesOnPage -> {
                    val itemsToSkip = (action.page - 1) * MovieRepository.MOVIES_PER_PAGE
                    val list: List<Movie>

                    // Checking if LocalRepository has requested data
                    if (localRepository.getMovies().items.size > itemsToSkip) {
                        // Have some data, return page items
                        list = localRepository.getMovies().items
                                .drop(itemsToSkip)
                                .take(MovieRepository.MOVIES_PER_PAGE)
                                .map {
                                    Movie(it.id,
                                            it.posterPath,
                                            it.originalTitle,
                                            it.overview,
                                            it.releaseDate,
                                            it.voteAverage)
                                }
                    } else {
                        // No local date, requesting from ApiRepository
                        list = apiRepository.getMovies(action.page)
                                .map {
                                    Movie(it.id,
                                            BuildConfig.BASE_IMAGE_URL + it.posterPath,
                                            it.originalTitle,
                                            it.overview,
                                            it.releaseDate,
                                            it.voteAverage)
                                }

                        localRepository.putMovies(list)
                    }

                    if (itemsToSkip == 0) {
                        results.accept(MovieRepository.Result.Movies(list))
                    } else {
                        results.accept(MovieRepository.Result.MoviesOnPage(list))
                    }
                }

                is MovieRepository.Action.GetMovieById -> {
                    // Searching for local repository
                    val list = localRepository.getMovies().items.filter { it.id == action.id }
                    if (list.isEmpty()) {
                        results.accept(MovieRepository.Result.Error(NoSuchElementException("Movie not found. ID: ${action.id}")))
                    } else if (list.size > 1) {
                        results.accept(MovieRepository.Result.Error(IllegalStateException("More then on movie found. ID: ${action.id}")))
                    } else {
                        val localMovie = list.first()
                        val movie = Movie(localMovie.id,
                                localMovie.posterPath,
                                localMovie.originalTitle,
                                localMovie.overview,
                                localMovie.releaseDate,
                                localMovie.voteAverage)
                        results.accept(MovieRepository.Result.MovieById(movie))
                    }
                }
            }

        }
    }

    override fun results(): Observable<MovieRepository.Result> = results
}