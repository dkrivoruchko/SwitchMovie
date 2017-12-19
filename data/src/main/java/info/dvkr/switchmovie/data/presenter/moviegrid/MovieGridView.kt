package info.dvkr.switchmovie.data.presenter.moviegrid

import android.support.annotation.Keep
import info.dvkr.switchmovie.data.presenter.BaseView
import info.dvkr.switchmovie.domain.model.Movie

interface MovieGridView : BaseView {

  @Keep data class MovieGridItem(val id: Int, val posterPath: String, val isStar: Boolean)

  // From MovieGridView to MovieGridPresenter
  @Keep sealed class FromEvent : BaseView.BaseFromEvent() {
    @Keep object RefreshItems : FromEvent()
    @Keep data class GetNext(val from: Int) : FromEvent()
    @Keep data class GetMovieById(val id: Int) : FromEvent()
    @Keep data class StarMovieById(val id: Int) : FromEvent()
  }

  // From MovieGridPresenter to MovieGridView
  @Keep sealed class ToEvent : BaseView.BaseToEvent() {
    @Keep data class OnMovieGridItemsRange(val range: Pair<Int, Int>, val list: List<MovieGridItem>) : ToEvent()
    @Keep data class OnMovie(val movie: Movie) : ToEvent()
    @Keep data class OnStarMovieById(val id: Int) : ToEvent()
    @Keep data class OnError(val error: Throwable) : ToEvent()
  }
}