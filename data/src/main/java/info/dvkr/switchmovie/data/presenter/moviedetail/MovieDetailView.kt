package info.dvkr.switchmovie.data.presenter.moviedetail

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie


interface MovieDetailView {

  // From MovieDetailView to MovieDetailPresenter
  @Keep sealed class FromEvent {
    @Keep data class GetMovieById(val id: Int) : FromEvent()
  }

  // From MovieDetailPresenter to MovieDetailView
  @Keep sealed class ToEvent {
    @Keep data class OnMovie(val movie: Movie) : ToEvent()
    @Keep data class OnError(val error: Throwable) : ToEvent()
  }

  // Events from MovieDetailPresenter to MovieDetailView
  fun toEvent(toEvent: ToEvent)
}