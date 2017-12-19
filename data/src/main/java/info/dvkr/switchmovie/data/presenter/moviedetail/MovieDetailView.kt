package info.dvkr.switchmovie.data.presenter.moviedetail

import android.support.annotation.Keep
import info.dvkr.switchmovie.data.presenter.BaseView
import info.dvkr.switchmovie.domain.model.Movie


interface MovieDetailView: BaseView {

  // From MovieDetailView to MovieDetailPresenter
  @Keep sealed class FromEvent: BaseView.BaseFromEvent() {
    @Keep data class GetMovieById(val id: Int) : FromEvent()
  }

  // From MovieDetailPresenter to MovieDetailView
  @Keep sealed class ToEvent: BaseView.BaseToEvent() {
    @Keep data class OnRefresh(val isRefreshing: Boolean) : ToEvent()
    @Keep data class OnMovie(val movie: Movie) : ToEvent()
    @Keep data class OnError(val error: Throwable) : ToEvent()
  }
}