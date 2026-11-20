package io.github.mirancz.libreinfo.activity.settings.departure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.settings.DepartureClickAction
import io.github.mirancz.libreinfo.activity.settings.DepartureSource
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.NavigationScreenScaffold
import io.github.mirancz.libreinfo.ui.components.CountdownConfirmDialog
import io.github.mirancz.libreinfo.ui.components.Divider
import io.github.mirancz.libreinfo.ui.components.NavigationItem
import io.github.mirancz.libreinfo.ui.components.SettingSwitch
import io.github.mirancz.libreinfo.util.AppSettings

class DepartureSettingsViewModel : ViewModel() {
    // read eagerly so the dropdown never flashes the default before load() lands
    var source by mutableStateOf(AppSettings.Departures.source)
        private set
    var clickAction by mutableStateOf(AppSettings.Departures.clickAction)
        private set

    fun load() {
        source = AppSettings.Departures.source
        clickAction = AppSettings.Departures.clickAction
    }

    fun updateSource(s: DepartureSource) {
        AppSettings.Departures.source = s
        source = s
    }

    fun updateClickAction(a: DepartureClickAction) {
        AppSettings.Departures.clickAction = a
        clickAction = a
    }

}


private const val CONFIRM_COOLDOWN_SECONDS = 3

@Composable
fun DepartureSettingsScreen(state: NavState) {
    val vm: DepartureSettingsViewModel = viewModel()

    LaunchedEffect(Unit) {
        vm.load()
    }

    NavigationScreenScaffold(stringResource(R.string.departures_settings), onBack = state.onBack) {
        DepartureSourceOption(vm)

        Divider()
        ClickActionOptions(vm)

        Spacer(Modifier.height(8.dp))

        NavigationItem(R.drawable.palette, stringResource(R.string.departures_theming)) {
            state.onNavigate(NavRoute.Settings.DepartureTheming)
        }
    }
}

@Composable
private fun DepartureSourceOption(vm: DepartureSettingsViewModel) {
    val sourceTranslationMap = mapOf(
        Pair(DepartureSource.SERVER, stringResource(R.string.departure_source_server)),
        Pair(DepartureSource.LOCAL, stringResource(R.string.departure_source_local)),
    )

    var pendingSource by remember { mutableStateOf<DepartureSource?>(null) }

    pendingSource?.let { pending ->
        LocalSourceWarningDialog(
            onConfirm = { vm.updateSource(pending); pendingSource = null },
            onDismiss = { pendingSource = null }
        )
    }

    SettingDropdown(
        stringResource(R.string.departure_source),
        vm.source,
        DepartureSource.entries,
        displayString = { sourceTranslationMap.getOrDefault(it, "??") }
    ) {
        // computing locally yields unofficial data, so that direction needs a confirmation
        if (it == DepartureSource.LOCAL && vm.source != DepartureSource.LOCAL)
            pendingSource = it else vm.updateSource(it)
    }
}

@Composable
private fun ClickActionOptions(vm: DepartureSettingsViewModel) {
    val sourceTranslationMap = mapOf(
        Pair(DepartureClickAction.MAP, stringResource(R.string.click_action_map)),
        Pair(DepartureClickAction.TRIP_DETAIL, stringResource(R.string.click_action_trip)),
    )

    Column() {
        SettingDropdown(
            stringResource(R.string.click_action_description),
            vm.clickAction,
            DepartureClickAction.entries,
            displayString = { sourceTranslationMap.getOrDefault(it, "??") }
        ) {
            vm.updateClickAction(it)
        }

        Divider()

        SettingSwitch(
            stringResource(R.string.click_action_fallback),
            AppSettings.Departures::clickFallback,
            enabled = vm.clickAction == DepartureClickAction.MAP
        )
    }
}

/**
 * Warns that on-device departures are unofficial. The confirm button unlocks only after
 * [CONFIRM_COOLDOWN_SECONDS] so the warning is not dismissed reflexively.
 */
@Composable
private fun LocalSourceWarningDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    CountdownConfirmDialog(
        stringResource(R.string.departure_source_warning_title),
        stringResource(R.string.cancel),
        stringResource(R.string.departure_source_warning_confirm),
        onDismiss,
        onConfirm,
        CONFIRM_COOLDOWN_SECONDS
    ) {
        Text(stringResource(R.string.departure_source_warning_message))
    }
}