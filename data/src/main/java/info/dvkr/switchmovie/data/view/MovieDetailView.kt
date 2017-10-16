package info.dvkr.switchmovie.data.view

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import io.reactivex.Observable


interface MovieDetailView {

    // From MovieDetailView to MovieDetailPresenter
    @Keep sealed class FromEvent {
        @Keep data class GetMovieById(val id: Int) : FromEvent()
    }

    // Events from MovieDetailView to MovieDetailPresenter
    fun fromEvent(): Observable<FromEvent>

    // From MovieDetailPresenter to MovieDetailView
    @Keep sealed class ToEvent {
        @Keep data class OnMovie(val movie: Movie) : ToEvent()
        @Keep data class OnError(val error: Throwable) : ToEvent()
    }

    // Events from MovieDetailPresenter to MovieDetailView
    fun toEvent(toEvent: ToEvent)
}