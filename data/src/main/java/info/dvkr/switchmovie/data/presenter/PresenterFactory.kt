package info.dvkr.switchmovie.data.presenter

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import info.dvkr.switchmovie.data.presenter.moviedetail.MovieDetailPresenter
import info.dvkr.switchmovie.data.presenter.moviegrid.MovieGridPresenter
import info.dvkr.switchmovie.domain.notifications.NotificationManager
import info.dvkr.switchmovie.domain.usecase.UseCases


class PresenterFactory constructor(private val useCases: UseCases,
                                   private val notificationManager: NotificationManager)
  : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MovieGridPresenter::class.java)) {
      return MovieGridPresenter(useCases, notificationManager) as T
    }

    if (modelClass.isAssignableFrom(MovieDetailPresenter::class.java)) {
      return MovieDetailPresenter(useCases, notificationManager) as T
    }


    throw  IllegalArgumentException("Unknown Presenter class")
  }
}