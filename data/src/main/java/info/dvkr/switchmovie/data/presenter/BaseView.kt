package info.dvkr.switchmovie.data.presenter

import android.support.annotation.Keep

interface BaseView {
    @Keep open class BaseFromEvent

    @Keep open class BaseToEvent {
        @Keep class OnProgress(val isWorkInProgress: Boolean) : BaseToEvent()
        @Keep class OnError(val error: Throwable) : BaseToEvent()
    }

    fun toEvent(toEvent: BaseToEvent)
}