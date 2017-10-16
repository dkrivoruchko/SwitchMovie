package info.dvkr.switchmovie.data.presenter

import android.util.Log
import info.dvkr.switchmovie.data.dagger.PersistentScope
import info.dvkr.switchmovie.data.view.MovieDetailView
import info.dvkr.switchmovie.domain.repository.MovieRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@PersistentScope
class MovieDetailPresenter @Inject internal constructor(private val movieRepository: MovieRepository) {

    private val disposables = CompositeDisposable()
    private var movieDetailActivity: MovieDetailView? = null

    init {
        Log.wtf("MovieDetailPresenter", "[${Thread.currentThread().name}] Create")
    }

    fun attach(activity: MovieDetailView) {
        movieDetailActivity?.let { detach() }
        movieDetailActivity = activity

        movieDetailActivity?.let {
            it.fromEvent().observeOn(Schedulers.single()).subscribe { fromEvent ->
                Log.wtf("MovieDetailPresenter", "[${Thread.currentThread().name}] fromEvent: $fromEvent")

                when (fromEvent) {
                    is MovieDetailView.FromEvent.GetMovieById -> {
                        movieRepository.actions(MovieRepository.Action.GetMovieById(fromEvent.id))
                    }
                }
            }.also { disposables.add(it) }

            movieRepository.results().observeOn(Schedulers.single()).subscribe { result ->
                Log.wtf("MovieDetailPresenter", "[${Thread.currentThread().name}] result: $result")

                when (result) {
                    is MovieRepository.Result.MovieById -> {
                        it.toEvent(MovieDetailView.ToEvent.OnMovie(result.movie))
                    }

                    is MovieRepository.Result.Error -> {
                        it.toEvent(MovieDetailView.ToEvent.OnError(result.error))
                    }
                }
            }.also { disposables.add(it) }
        }
    }

    fun detach() {
        disposables.clear()
        movieDetailActivity = null
    }
}