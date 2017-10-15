package info.dvkr.switchmovie.data.view

import android.support.annotation.Keep
import info.dvkr.switchmovie.domain.model.Movie
import io.reactivex.Observable


interface MovieDetailView {
    // From MovieGridView to MovieGridPresenter
    @Keep sealed class FromEvent {
        @Keep data class GetMovieById(val id: Int) : FromEvent()
    }

    // Events from MovieGridView to MovieGridPresenter
    fun fromEvent(): Observable<FromEvent>

    // From MovieGridPresenter to MovieGridView
    @Keep sealed class ToEvent {
        @Keep data class OnMovie(val movie: Movie) : ToEvent()
        @Keep data class OnError(val error: Throwable) : ToEvent()
    }

    // Events from MovieGridPresenter to MovieGridView
    fun toEvent(toEvent: ToEvent)
}