package info.dvkr.switchmovie.data.presenter

import android.util.Log
import info.dvkr.switchmovie.data.dagger.PersistentScope
import info.dvkr.switchmovie.data.view.MovieGridView
import info.dvkr.switchmovie.domain.repository.MovieRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@PersistentScope
class MovieGridPresenter @Inject internal constructor(private val movieRepository: MovieRepository) {

    private val disposables = CompositeDisposable()
    private var movieGridActivity: MovieGridView? = null

    init {
        Log.wtf("MovieGridPresenter", "Thread [${Thread.currentThread().name}] Create")
    }

    fun attach(activity: MovieGridView) {
        movieGridActivity?.let { detach() }
        movieGridActivity = activity

        movieGridActivity?.let {
            it.fromEvent().observeOn(Schedulers.single()).subscribe { fromEvent ->
                Log.wtf("MovieGridPresenter", "[${Thread.currentThread().name}] fromEvent: $fromEvent")

                when (fromEvent) {
                    is MovieGridView.FromEvent.RefreshItems -> {
                        it.toEvent(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.actions(MovieRepository.Action.GetMoviesOnPage(1))
                    }

                    is MovieGridView.FromEvent.GetPage -> {
                        it.toEvent(MovieGridView.ToEvent.OnRefresh(true))
                        movieRepository.actions(MovieRepository.Action.GetMoviesOnPage(fromEvent.page))
                    }
                }
            }.also { disposables.add(it) }

            movieRepository.results().observeOn(Schedulers.single()).subscribe { result ->
                Log.wtf("MovieGridPresenter", "[${Thread.currentThread().name}] result: $result")

                when (result) {
                    is MovieRepository.Result.Movies -> {
                        val list = result.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath) }
                        it.toEvent(MovieGridView.ToEvent.OnMovieGridItemsRefresh(list))
                        it.toEvent(MovieGridView.ToEvent.OnRefresh(false))
                    }

                    is MovieRepository.Result.MoviesOnPage -> {
                        val list = result.moviesList.map { MovieGridView.MovieGridItem(it.id, it.posterPath) }
                        it.toEvent(MovieGridView.ToEvent.OnMovieGridItemsPage(list))
                        it.toEvent(MovieGridView.ToEvent.OnRefresh(false))
                    }

                    is MovieRepository.Result.Error -> {
                        it.toEvent(MovieGridView.ToEvent.OnError(result.error))
                        it.toEvent(MovieGridView.ToEvent.OnRefresh(false))
                    }
                }
            }.also { disposables.add(it) }
        }
    }

    fun detach() {
        disposables.clear()
        movieGridActivity = null
    }
}