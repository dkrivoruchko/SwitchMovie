package info.dvkr.switchmovie.data.presenter

import android.support.annotation.Keep
import android.support.annotation.UiThread

interface BaseView {
  @Keep open class BaseFromEvent

  @Keep open class BaseToEvent {
    @Keep data class OnRefresh(val isRefreshing: Boolean) : BaseToEvent()
  }

  @UiThread
  fun toEvent(toEvent: BaseToEvent)
}