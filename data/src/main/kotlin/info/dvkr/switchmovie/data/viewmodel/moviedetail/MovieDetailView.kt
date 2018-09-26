package info.dvkr.switchmovie.data.viewmodel.moviedetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie


@Keep data class MovieDetailModel(
    val movieLiveData: LiveData<Movie> = MediatorLiveData(),
    val workInProgressCounter: Int = 0,
    val error: Throwable? = null
)

@Keep sealed class MovieDetailEvent {
    // Event from View
    @Keep data class ViewGetMovieById(val movieId: Int) : MovieDetailEvent()

    // Event in ViewModel
    @Keep data class OnMovie(val movieLiveData: LiveData<Movie>) : MovieDetailEvent()
    @Keep object WorkStart : MovieDetailEvent()
    @Keep object WorkFinish : MovieDetailEvent()
    @Keep data class Error(val error: Throwable) : MovieDetailEvent()
}

@Keep sealed class MovieDetailEffect {
    @Keep data class GetMovieById(val movieId: Int) : MovieDetailEffect()
}