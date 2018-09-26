package info.dvkr.switchmovie.data.utils

import com.ironz.binaryprefs.Preferences
import com.ironz.binaryprefs.PreferencesEditor
import com.ironz.binaryprefs.serialization.serializer.persistable.Persistable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
fun <T> bindPreference(preferences: Preferences, key: String, defaultValue: T): ReadWriteProperty<Any, T> =
    when (defaultValue) {
        is Boolean -> PreferenceDelegate(
            preferences, key, defaultValue, Preferences::getBoolean, PreferencesEditor::putBoolean
        )

        is Int -> PreferenceDelegate(
            preferences, key, defaultValue, Preferences::getInt, PreferencesEditor::putInt
        )

        is Long -> PreferenceDelegate(
            preferences, key, defaultValue, Preferences::getLong, PreferencesEditor::putLong
        )

        is Float -> PreferenceDelegate(
            preferences, key, defaultValue, Preferences::getFloat, PreferencesEditor::putFloat
        )

        is String -> PreferenceDelegate(
            preferences, key, defaultValue, Preferences::getString, PreferencesEditor::putString
        )

        is Persistable -> PreferenceDelegate(
            preferences, key, defaultValue, Preferences::getPersistable, PreferencesEditor::putPersistable
        )

        else -> throw IllegalArgumentException("Unsupported preference type")
    } as ReadWriteProperty<Any, T>

private class PreferenceDelegate<T>(
    private val preferences: Preferences,
    private val key: String,
    private val defaultValue: T,
    private val getter: Preferences.(String, T) -> T,
    private val setter: PreferencesEditor.(String, T) -> PreferencesEditor
) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        preferences.getter(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        preferences.edit().setter(key, value).commit()
    }
}