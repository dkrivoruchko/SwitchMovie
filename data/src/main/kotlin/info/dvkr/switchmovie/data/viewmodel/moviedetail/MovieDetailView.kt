package info.dvkr.switchmovie.data.viewmodel.moviedetail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.support.annotation.Keep
import info.dvkr.switchmovie.data.viewmodel.BaseView
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.Job


@Keep data class MovieDetailModel(
    val movieLiveData: LiveData<Movie> = MediatorLiveData(),
    val isWorkInProgress: Boolean = false,
    val error: Throwable? = null
)


@Keep sealed class MovieDetailEvent : BaseView.BaseViewEvent() {
    // Event from View

    @Keep data class ViewGetMovieById(val movieId: Int) : MovieDetailEvent()
    @Keep data class ViewInvertMovieStarById(val movieId: Int) : MovieDetailEvent()

    // Event in ViewModel

    @Keep data class OnMovie(val movieLiveData: LiveData<Movie>) : MovieDetailEvent()
    @Keep data class InvertMovieStarByIdDone(val movieId: Int) : MovieDetailEvent()
    @Keep data class Error(val error: Throwable) : MovieDetailEvent()
}

@Keep sealed class MovieDetailEffect {
    @Keep data class GetMovieById(val movieId: Int) : MovieDetailEffect()
//    @Keep data class InvertMovieStarById(val invertMovieStarByIdJob: Pair<Int, Job>) : MovieDetailEffect()
}