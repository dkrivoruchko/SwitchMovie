package info.dvkr.switchmovie.viewmodel.moviegrid

import info.dvkr.switchmovie.viewmodel.BaseViewModel

sealed class MovieGridViewEvent : BaseViewModel.Event {
    object Refresh : MovieGridViewEvent()
    object LoadMore : MovieGridViewEvent()
    object Update : MovieGridViewEvent()
    data class SetMovieStar(val movieId: Long) : MovieGridViewEvent()
    data class UnsetMovieStar(val movieId: Long) : MovieGridViewEvent()

    override fun toString(): String = this::class.java.simpleName
}