package info.dvkr.switchmovie.data.settings

import com.ironz.binaryprefs.Preferences
import info.dvkr.switchmovie.data.utils.bindPreference
import info.dvkr.switchmovie.domain.settings.Settings
import info.dvkr.switchmovie.domain.utils.getTag
import timber.log.Timber


class SettingsImpl(preferences: Preferences) : Settings {

    override var example: Long by bindPreference(
        preferences, "PREF_KEY_EXAMPLE", 0L
    )

    init {
        Timber.tag(getTag()).d("Init")

        example = 0L
    }
}