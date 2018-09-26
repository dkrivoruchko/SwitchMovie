package info.dvkr.switchmovie.data.viewmodel.moviegrid

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import kotlinx.coroutines.experimental.Job


@Keep data class MovieGridModel(
    val moviesLiveData: LiveData<List<Movie>> = MediatorLiveData(),
    val invertMovieStarByIdJob: Pair<Int, Job>? = null,
    val workInProgressCounter: Int = 0,
    val error: Throwable? = null
)

@Keep sealed class MovieGridEvent {
    // Event from View
    @Keep data class ViewCancelInvertMovieStarById(val movieId: Int) : MovieGridEvent()
    @Keep object ViewRefresh : MovieGridEvent()
    @Keep object ViewUpdate : MovieGridEvent()
    @Keep object ViewLoadMore : MovieGridEvent()
    @Keep data class ViewInvertMovieStarById(val movieId: Int) : MovieGridEvent()

    // Event in ViewModel
    @Keep object Init : MovieGridEvent()
    @Keep data class OnMovieList(val moviesLiveData: LiveData<List<Movie>>) : MovieGridEvent()
    @Keep data class InvertMovieStarByIdDone(val movieId: Int) : MovieGridEvent()
    @Keep object WorkStart : MovieGridEvent()
    @Keep object WorkFinish : MovieGridEvent()
    @Keep data class Error(val error: Throwable) : MovieGridEvent()
}

@Keep sealed class MovieGridEffect {
    @Keep object Init : MovieGridEffect()
    @Keep object Refresh : MovieGridEffect()
    @Keep object Update : MovieGridEffect()
    @Keep object LoadMore : MovieGridEffect()
    @Keep data class InvertMovieStarById(val invertMovieStarByIdJob: Pair<Int, Job>) : MovieGridEffect()
}

data class MovieGridViewItem(val id: Int, val posterPath: String, val isStar: Boolean) {
    override fun toString() = "Movie(id=$id)"
}