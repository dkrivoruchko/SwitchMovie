package info.dvkr.switchmovie.data.viewmodel.moviedetail

import info.dvkr.switchmovie.data.viewmodel.BaseViewModel


sealed class MovieDetailViewEvent : BaseViewModel.Event {
    data class GetMovieById(val movieId: Int) : MovieDetailViewEvent()
}