package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailPresenter
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridPresenter
import info.dvkr.switchmovie.domain.repository.MovieRepository
import kotlinx.coroutines.experimental.ThreadPoolDispatcher


class PresenterFactory constructor(private val presenterContext: ThreadPoolDispatcher,
                                   private val movieRepository: MovieRepository)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieGridPresenter::class.java)) {
            return MovieGridPresenter(presenterContext, movieRepository) as T
        }

        if (modelClass.isAssignableFrom(MovieDetailPresenter::class.java)) {
            return MovieDetailPresenter(presenterContext, movieRepository) as T
        }


        throw  IllegalArgumentException("Unknown Presenter class")
    }
}