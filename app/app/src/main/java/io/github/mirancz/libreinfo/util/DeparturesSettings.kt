package io.github.mirancz.libreinfo.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mirancz.libreinfo.activity.settings.DelayRenderType

data class DeparturesSettings(
    val delayRender: DelayRenderType = DelayRenderType.DEFAULT,
    val showLowFloor: Boolean = true,
    val maxEntries: Int = 5
) {
    companion object {
        var current by mutableStateOf(fromPrefs())
            private set

        fun fromPrefs() = DeparturesSettings(
            delayRender = AppSettings.Departures.delayRender,
            showLowFloor = AppSettings.Departures.showLowFloor,
            maxEntries = AppSettings.Departures.maxEntries
        )

        fun save(s: DeparturesSettings) {
            AppSettings.edit {
                AppSettings.Departures.delayRender = s.delayRender
                AppSettings.Departures.showLowFloor = s.showLowFloor
                AppSettings.Departures.maxEntries = s.maxEntries
            }
            current = s
        }
    }
}

val LocalDeparturesSettings = compositionLocalOf { DeparturesSettings() }
