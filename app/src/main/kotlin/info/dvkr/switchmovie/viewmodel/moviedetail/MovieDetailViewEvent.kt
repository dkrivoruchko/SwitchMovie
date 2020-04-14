package info.dvkr.switchmovie.viewmodel.moviedetail

import info.dvkr.switchmovie.viewmodel.BaseViewModel


sealed class MovieDetailViewEvent : BaseViewModel.Event {
    data class GetMovieById(val movieId: Int) : MovieDetailViewEvent()
}