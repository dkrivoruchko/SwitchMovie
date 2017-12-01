package info.dvkr.switchmovie.data.presenter.moviegrid

import android.support.annotation.Keep

interface MovieGridView {

    @Keep data class MovieGridItem(val id: Int, val posterPath: String)

    // From MovieGridView to MovieGridPresenter
    @Keep sealed class FromEvent {
        @Keep object RefreshItems : FromEvent()
        @Keep data class GetPage(val page: Int) : FromEvent()
    }

    // From MovieGridPresenter to MovieGridView
    @Keep sealed class ToEvent {
        @Keep data class OnRefresh(val isRefreshing: Boolean) : ToEvent()
        @Keep data class OnMovieGridItemsRefresh(val list: List<MovieGridItem>) : ToEvent()
        @Keep data class OnMovieGridItemsPage(val list: List<MovieGridItem>) : ToEvent()
        @Keep data class OnError(val error: Throwable) : ToEvent()
    }

    // Events from MovieGridPresenter to MovieGridView
    fun toEvent(toEvent: ToEvent)
}