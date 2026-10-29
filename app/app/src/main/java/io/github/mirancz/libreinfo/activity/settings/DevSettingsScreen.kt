package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.NavigationScreenScaffold
import io.github.mirancz.libreinfo.ui.components.NavigationItem

@Composable
fun DevSettingsScreen(state: NavState) {
    NavigationScreenScaffold(stringResource(R.string.settings), onBack = state.onBack) {
        NavigationItem(R.drawable.code, "Line test") { state.onNavigate(NavRoute.Settings.Dev.LineList) }
        NavigationItem(R.drawable.code, "Departure perf test") { state.onNavigate(NavRoute.Settings.Dev.DeparturePerformance) }
    }
}