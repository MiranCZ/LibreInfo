package io.github.mirancz.libreinfo.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.attribution.AttributionScreen
import io.github.mirancz.libreinfo.activity.devtest.DeparturePerformanceActivity
import io.github.mirancz.libreinfo.activity.devtest.LineListScreen
import io.github.mirancz.libreinfo.activity.settings.DeparturesSettingsScreen
import io.github.mirancz.libreinfo.activity.settings.DevSettingsScreen
import io.github.mirancz.libreinfo.activity.settings.LocationSettingsScreen
import io.github.mirancz.libreinfo.activity.settings.SettingsScreen
import io.github.mirancz.libreinfo.activity.settings.UpdatingSettingsScreen
import io.github.mirancz.libreinfo.nav.LocalNavResults
import io.github.mirancz.libreinfo.nav.NavResults
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.AppRoot
import io.github.mirancz.libreinfo.ui.NavigationScreenScaffold
import io.github.mirancz.libreinfo.ui.components.AppButton
import io.github.mirancz.libreinfo.ui.components.ConfirmDialog
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.ui.components.NavigationItem
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import io.github.mirancz.libreinfo.util.ApkInstaller
import io.github.mirancz.libreinfo.util.AppUpdater
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.UpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoot {
                CompositionLocalProvider(LocalDeparturesSettings provides DeparturesSettings.current) {
                    AppNavHost()
                }
            }
        }
    }

}

@Composable
fun HomeScreen(onNavigate: (NavRoute) -> Unit) {
    NavigationScreenScaffold(
        title = stringResource(R.string.app_name),
        onBack = null,
        bottomOverlay = { UpdateOverlay() },
    ) {
        NavigationItem(
            R.drawable.bus_light_full,
            R.string.departures
        ) { onNavigate(NavRoute.StopSearch(prefetchDelays = true)) }
        NavigationItem(
            R.drawable.location_arrow,
            R.string.vehicle_map,
            true
        ) { onNavigate(NavRoute.VehicleMap) }
        NavigationItem(R.drawable.map_regular_full, R.string.connection_search, true) {
            onNavigate(
                NavRoute.ConnectionSearch
            )
        }
        NavigationItem(
            R.drawable.list_ul_regular,
            R.string.vehicles,
            true
        ) { onNavigate(NavRoute.VehiclesList) }
        NavigationItem(
            R.drawable.bolt_regular,
            R.string.events,
            true
        ) { onNavigate(NavRoute.Events) }
        NavigationItem(
            R.drawable.triangle_exclamation_regular,
            R.string.diversions,
            true
        ) { onNavigate(NavRoute.Diversions) }
        NavigationItem(
            R.drawable.message_lines_regular,
            R.string.news,
            true
        ) { onNavigate(NavRoute.News) }
//        NavigationItem(R.drawable.address_card_regular, "Šalinkarta") {}
//        NavigationItem(R.drawable.code_fork_regular, R.string.schemes) {}
        NavigationItem(
            R.drawable.gear_regular,
            R.string.settings
        ) { onNavigate(NavRoute.Settings) }
        NavigationItem(
            R.drawable.circle_info_regular,
            R.string.about
        ) { onNavigate(NavRoute.About) }
    }
}

@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(NavRoute.serializer())) {
        NavBackStack(NavRoute.Home)
    }

    val decelerateQuad = Easing { 1f - (1f - it) * (1f - it) }
    val accelerateQuad = Easing { it * it }
    val accelerateCubic = Easing { it * it * it }

    val pushTransition = scaleIn(tween(240, easing = decelerateQuad), initialScale = 0.9f) +
            fadeIn(tween(240, easing = decelerateQuad), initialAlpha = 0.25f) togetherWith
            fadeOut(tween(220, easing = accelerateQuad))
    val popTransition = fadeIn(tween(150, easing = decelerateQuad)) togetherWith
            scaleOut(tween(140, easing = accelerateCubic), targetScale = 0.8f) +
            fadeOut(tween(140, easing = accelerateCubic), targetAlpha = 0.25f)

    fun navigate(route: NavRoute) {
        val activity = when (route) {
            NavRoute.VehicleMap -> VehicleMapActivity::class
            NavRoute.Settings.Dev.DeparturePerformance -> DeparturePerformanceActivity::class
            else -> null
        }

        if (activity != null) {
            context.startActivity(Intent(context, activity.java))
        } else {
            backStack.add(route)
        }
    }

    // Navigation is only allowed from the topmost entry, this prevents stepping back (or forward) multiple times whilst
    // the exit animation is playing
    fun stateFor(route: NavRoute) = NavState(
        onNavigate = { if (backStack.lastOrNull() === route) navigate(it) },
        onBack = { if (backStack.size > 1 && backStack.last() === route) backStack.removeAt(backStack.lastIndex) }
    )

    val results = remember { NavResults() }

    CompositionLocalProvider(LocalNavResults provides results) {
        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = { pushTransition },
            popTransitionSpec = { popTransition },
            predictivePopTransitionSpec = { popTransition },
            entryProvider = entryProvider {
                entry<NavRoute.Home> { HomeScreen(onNavigate = stateFor(it).onNavigate) }

                entry<NavRoute.Settings> { SettingsScreen(stateFor(it)) }
                entry<NavRoute.ConnectionSearch> { ConnectionSearchScreen(stateFor(it)) }
                entry<NavRoute.ConnectionResults> {
                    ConnectionsResultScreen(stateFor(it), it.fromId, it.toId, it.time, it.isArrival)
                }

                entry<NavRoute.Settings.Location> { LocationSettingsScreen(stateFor(it)) }
                entry<NavRoute.Settings.Updates> { UpdatingSettingsScreen(stateFor(it)) }
                entry<NavRoute.Settings.Departures> { DeparturesSettingsScreen(stateFor(it)) }

                entry<NavRoute.News> { NewsScreen(stateFor(it)) }
                entry<NavRoute.About> { AboutScreen(stateFor(it)) }
                entry<NavRoute.About.Attribution> { AttributionScreen(stateFor(it)) }
                entry<NavRoute.VehiclesList> { VehiclesListScreen(stateFor(it)) }
                entry<NavRoute.Events> { EventsScreen(stateFor(it)) }

                entry<NavRoute.Diversions> { DiversionsScreen(stateFor(it)) }

                entry<NavRoute.Diversions.Detail> { DiversionDetailScreen(stateFor(it), it.diversion) }

                // FIXME pass only IDs instead
                entry<NavRoute.News.Detail> { NewsDetailScreen(stateFor(it), it.entry) }

                @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
                if (BuildConfig.BUILD_TYPE != "release") {
                    entry<NavRoute.Settings.Dev> { DevSettingsScreen(stateFor(it)) }

                    entry<NavRoute.Settings.Dev.LineList> { LineListScreen(stateFor(it)) }
                }


                entry<NavRoute.StopSearch> { route ->
                    val state = stateFor(route)
                    SearchScreen<SearchOption.PickedStop>(state, prefetchDelays = route.prefetchDelays) { picked ->
                        state.onNavigate(NavRoute.Departures(picked.stop.id.internal))
                    }
                }

                entry<NavRoute.Departures> { DeparturesScreen(stateFor(it), it.stopId) }
                entry<NavRoute.Departures.PostDetail> { DeparturePostDetailScreen(stateFor(it), it.stopId, it.postId) }

                entry<NavRoute.TripDetail> { TripDetailScreen(stateFor(it), it.vehicleId, it.stopId, it.routeId) }

                entry<NavRoute.StopPicker> { route ->
                    val state = stateFor(route)

                    SearchScreen(state, route.kinds) { picked ->
                        results.send(route.resultKey, picked)
                        state.onBack()
                    }
                }
            }
        )
    }
}

@Composable
fun UpdateOverlay(modifier: Modifier = Modifier) {
    if (!BuildConfig.AUTO_UPDATE_ENABLED) return

    val context = LocalContext.current
    var updateReady by rememberSaveable { mutableStateOf(false) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var showUpdatePrompt by rememberSaveable { mutableStateOf(!AppUpdater.isFirstRunPromptShown()) }

    LaunchedEffect(Unit) {
        updateReady = withContext(Dispatchers.IO) {
            UpdateHelper.isUpdateDownloaded(context)
        }
    }

    if (showUpdatePrompt) {
        AutoUpdatePromptDialog(
            onChoice = { enabled ->
                AppUpdater.markFirstRunPromptShown()
                AppUpdater.setAutoUpdateEnabled(context, enabled)
                showUpdatePrompt = false
            }
        )
    }

    // Returning from the "install unknown apps" settings screen; if the user granted it continue straight into the installation they originally asked for
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (ApkInstaller.installAllowed(context)) {
            showRationale = false
            ApkInstaller.launchInstall(context)
        }
    }

    if (updateReady && !dismissed) {
        UpdateBanner(
            modifier,
            onInstall = {
                dismissed = true
                if (ApkInstaller.installAllowed(context)) {
                    ApkInstaller.launchInstall(context)
                } else {
                    showRationale = true
                }
            },
            onDismiss = { dismissed = true }
        )
    }

    if (showRationale) {
        InstallPermissionDialog(
            onContinue = { settingsLauncher.launch(ApkInstaller.unknownSourcesIntent(context)) },
            onDismiss = { showRationale = false }
        )
    }

    if (ApkInstaller.preparing) {
        PreparingDialog()
    }

    val installError = ApkInstaller.lastError
    if (installError != null) {
        InstallFailedDialog(detail = installError, onDismiss = { ApkInstaller.clearError() })
    }
}

@Composable
private fun PreparingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Container(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    Modifier.size(24.dp),
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.update_preparing))
            }
        }
    }
}

@Composable
private fun UpdateBanner(
    modifier: Modifier = Modifier,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    Container(modifier = modifier.padding(16.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.update_ready),
                fontWeight = FontWeight.Medium
            )

            Row(horizontalArrangement = Arrangement.Center) {
                AppButton(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(4.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    onClick = onDismiss
                ) {
                    Text(
                        stringResource(R.string.close),
                        color = MaterialTheme.extendedColors.onSurfaceMedium
                    )
                }

                AppButton(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onInstall
                ) {
                    Text(stringResource(R.string.install))

                }
            }
        }
    }
}

@Composable
private fun AutoUpdatePromptDialog(onChoice: (Boolean) -> Unit) {
    ConfirmDialog(
        stringResource(R.string.update_prompt_title),
        stringResource(R.string.update_prompt_decline),
        stringResource(R.string.update_prompt_enable),
        { onChoice(false) },
        { onChoice(true) }
    ) {
        Text(stringResource(R.string.update_prompt_message))
    }
}

@Composable
fun InstallPermissionDialog(onContinue: () -> Unit, onDismiss: () -> Unit) {
    ConfirmDialog(
        stringResource(R.string.install_permission_title),
        stringResource(R.string.cancel),
        stringResource(R.string.continue_action),
        onDismiss,
        onContinue
    ) {
        Text(stringResource(R.string.install_permission_rationale))
    }
}

@Composable
private fun InstallFailedDialog(detail: String, onDismiss: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Container {
            Column {
                Text(
                    stringResource(R.string.install_failed_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(stringResource(R.string.install_failed_message))

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.debug_info),
                        color = MaterialTheme.extendedColors.onSurfaceMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.extendedColors.onSurfaceMedium
                    )
                }

                if (expanded) {
                    Spacer(Modifier.height(4.dp))
                    SelectionContainer {
                        Text(
                            detail,
                            color = MaterialTheme.extendedColors.onSurfaceMedium,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}


