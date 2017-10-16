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

                    // Checking if LocalRepository has requested data
                    localRepository.getMovies()
                            .observeOn(Schedulers.single())
                            .skip(itemsToSkip.toLong())
                            .take(MovieRepository.MOVIES_PER_PAGE.toLong())
                            .map { Movie(it.id, it.posterPath, it.originalTitle, it.overview, it.releaseDate, it.voteAverage) }
                            .toList() // List of Local data
                            .flatMap { movieList ->
                                if (!movieList.isEmpty()) { // Have Local data
                                    Single.just(movieList)
                                } else { // No local data, requesting from ApiRepository
                                    apiRepository.getMovies(action.page)
                                            .observeOn(Schedulers.single())
                                            .map {
                                                Movie(it.id,
                                                        BuildConfig.BASE_IMAGE_URL + it.posterPath,
                                                        it.originalTitle,
                                                        it.overview,
                                                        it.releaseDate,
                                                        it.voteAverage)
                                            }
                                            .toList()
                                            .doOnSuccess { localRepository.putMovies(it) } // Saving to Local
                                }
                            }
                            .subscribe(
                                    { movieList ->
                                        if (itemsToSkip == 0) {
                                            results.accept(MovieRepository.Result.Movies(movieList))
                                        } else {
                                            results.accept(MovieRepository.Result.MoviesOnPage(movieList))
                                        }
                                    },
                                    { error -> results.accept(MovieRepository.Result.Error(error)) }
                            )
                }

                is MovieRepository.Action.GetMovieById -> {
                    // Searching for local repository
                    localRepository.getMovies()
                            .observeOn(Schedulers.single())
                            .filter { it.id == action.id }
                            .singleOrError()
                            .map { Movie(it.id, it.posterPath, it.originalTitle, it.overview, it.releaseDate, it.voteAverage) }
                            .subscribe(
                                    { movie -> results.accept(MovieRepository.Result.MovieById(movie)) },
                                    { error -> results.accept(MovieRepository.Result.Error(IllegalStateException("Movie not found. ID: ${action.id}"))) }
                            )
                }
            }
        }
    }

    override fun results(): Observable<MovieRepository.Result> = results
}