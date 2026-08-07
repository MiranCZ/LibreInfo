package io.github.mirancz.libreinfo.util

import io.github.mirancz.libreinfo.activity.settings.DelayRenderType
import io.github.mirancz.libreinfo.activity.settings.DepartureSource
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object AppSettings {

    object Location {
        var distanceSort by boolPref("sort_stops_by_distance", false)
        var locationPermissionsRequested by boolPref("location_permissions_requested", false)
    }

    object Diversions {
        var applyFilters by boolPref("apply_filters", false)
        var diversionFilters by intListPref("diversion_filters", emptyList())
    }

    object Departures {
        var source by enumPref("dep_source", DepartureSource::class.java, DepartureSource.DEFAULT)
        var delayRender by enumPref("dep_delay_render", DelayRenderType::class.java, DelayRenderType.DEFAULT)
        var showLowFloor by boolPref("dep_show_low_floor", true)
        var maxEntries by intPref("dep_max_entries", 5)
    }

    // Consumed from Java (AppUpdater), hence @JvmStatic
    object Updates {
        @get:JvmStatic @set:JvmStatic
        var autoUpdateEnabled by boolPref("app_auto_update_enabled", false)

        @get:JvmStatic @set:JvmStatic
        var updateLastCheck by longPref("app_update_last_check", 0L)

        @get:JvmStatic @set:JvmStatic
        var updatePromptShown by boolPref("app_update_prompt_shown", false)
    }

    /**
     * Stages the setting writes inside [block] and flushes them to disk once, when the block
     * ends, instead of flushing after each assignment.

     * ```
     * AppSettings.edit {
     *     AppSettings.Departures.showLowFloor = true
     *     AppSettings.Departures.maxEntries = 8
     * } // single flush
     * ```
     */
    fun edit(block: () -> Unit) {
        batchDepth++
        try {
            block()
        } finally {
            batchDepth--
            Settings.get().flush()
        }
    }
}

/** Nesting depth of active [AppSettings.edit] blocks; while > 0, [Pref] defers per-write flushing. */
private var batchDepth = 0

private class Pref<T>(
    private val read: (PreferencesHolder) -> T,
    private val write: (PreferencesHolder, T) -> Unit,
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = read(Settings.get())

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val holder = Settings.get()
        write(holder, value)
        if (batchDepth == 0) holder.flush()
    }
}

private fun boolPref(key: String, default: Boolean): ReadWriteProperty<Any?, Boolean> =
    Pref({ it.getBoolean(key, default) }, { h, v -> h.putBoolean(key, v) })

private fun intPref(key: String, default: Int): ReadWriteProperty<Any?, Int> =
    Pref({ it.getInt(key, default) }, { h, v -> h.putInt(key, v) })

private fun longPref(key: String, default: Long): ReadWriteProperty<Any?, Long> =
    Pref({ it.getLong(key, default) }, { h, v -> h.putLong(key, v) })

private fun intListPref(key: String, default: List<Int>): ReadWriteProperty<Any?, List<Int>> =
    Pref({ it.getIntList(key, default) }, { h, v -> h.putIntList(key, v) })

private fun <T : Enum<T>> enumPref(key: String, clazz: Class<T>, default: T): ReadWriteProperty<Any?, T> =
    Pref({ it.getEnum(key, clazz, default) }, { h, v -> h.putEnum(key, v) })
