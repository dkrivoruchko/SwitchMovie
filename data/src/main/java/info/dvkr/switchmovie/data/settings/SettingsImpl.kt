package info.dvkr.switchmovie.data.settings

import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.data.utils.bindPreference
import info.dvkr.switchmovie.domain.settings.Settings
import timber.log.Timber


class SettingsImpl(preferences: Preferences) : Settings {

  override var example: Boolean by bindPreference(preferences, "PREF_KEY_EXAMPLE", true)

  init {
    Timber.i("[${this.javaClass.simpleName}#${this.hashCode()}@${Thread.currentThread().name}] Init")

    example = false
  }
}