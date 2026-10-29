package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.NavigationScreenScaffold
import io.github.mirancz.libreinfo.ui.components.NavigationItem


@Composable
fun SettingsScreen(state: NavState) {
    NavigationScreenScaffold(stringResource(R.string.settings), onBack = state.onBack) {
        NavigationItem(
            R.drawable.palette, R.string.departures_settings
        ) { state.onNavigate(NavRoute.Settings.Departures) }

        NavigationItem(
            R.drawable.location_dot, R.string.location
        ) { state.onNavigate(NavRoute.Settings.Location) }

        @Suppress("KotlinConstantConditions")
        if (BuildConfig.AUTO_UPDATE_ENABLED) {
            NavigationItem(
                R.drawable.download, R.string.updating_settings
            ) { state.onNavigate(NavRoute.Settings.Updates) }
        }

        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (BuildConfig.BUILD_TYPE != "release") {
            NavigationItem(R.drawable.code, "dev options") { state.onNavigate(NavRoute.Settings.Dev) }
        }
    }
}