package me.miran.libreinfo.util

import androidx.compose.runtime.compositionLocalOf
import me.miran.libreinfo.activity.settings.DelayRenderType

private const val KEY_DELAY_RENDER = "dep_delay_render"
private const val KEY_SHOW_LOW_FLOOR = "dep_show_low_floor"
private const val KEY_MAX_ENTRIES = "dep_max_entries"

data class DeparturesSettings(
    val delayRender: DelayRenderType = DelayRenderType.BOX,
    val showLowFloor: Boolean = true,
    val maxEntries: Int = 5
) {
    companion object {
        fun fromPrefs(): DeparturesSettings {
            val p = Settings.get()
            return DeparturesSettings(
                delayRender = p.getEnum(
                    KEY_DELAY_RENDER,
                    DelayRenderType::class.java,
                    DelayRenderType.BOX
                ),
                showLowFloor = p.getBoolean(KEY_SHOW_LOW_FLOOR, true),
                maxEntries = p.getInt(KEY_MAX_ENTRIES, 5)
            )
        }

        fun save(s: DeparturesSettings) {
            Settings.get()
                .putEnum(KEY_DELAY_RENDER, s.delayRender)
                .putBoolean(KEY_SHOW_LOW_FLOOR, s.showLowFloor)
                .putInt(KEY_MAX_ENTRIES, s.maxEntries)
                .flush()
        }
    }
}

val LocalDeparturesSettings = compositionLocalOf { DeparturesSettings() }
