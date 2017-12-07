package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailViewModel
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridViewModel
import info.dvkr.switchmovie.domain.repository.MovieRepository


class ViewModelFactory constructor(private val movieRepository: MovieRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieGridViewModel::class.java)) {
            return MovieGridViewModel(movieRepository) as T
        }

        if (modelClass.isAssignableFrom(MovieDetailViewModel::class.java)) {
            return MovieDetailViewModel(movieRepository) as T
        }


        throw  IllegalArgumentException("Unknown Presenter class")
    }
}