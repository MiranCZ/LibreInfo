package io.github.mirancz.libreinfo.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.devtest.DeparturePerformanceActivity
import io.github.mirancz.libreinfo.activity.devtest.LineListActivity
import io.github.mirancz.libreinfo.activity.settings.DeparturesSettingsActivity
import io.github.mirancz.libreinfo.activity.settings.DevSettingsScreen
import io.github.mirancz.libreinfo.activity.settings.LocationSettingsScreen
import io.github.mirancz.libreinfo.activity.settings.SettingsScreen
import io.github.mirancz.libreinfo.activity.settings.UpdatingSettingsActivity
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
import io.github.mirancz.libreinfo.util.UpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot { AppNavHost() } }
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
        ) { onNavigate(NavRoute.Search) }
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
fun AppNavHost(nav: NavHostController = rememberNavController()) {
    NavHost(
        nav, startDestination = NavRoute.Home,
        enterTransition = {
            scaleIn(initialScale = 0.9f, animationSpec = tween(340)) +
            fadeIn(animationSpec = tween(350))
        },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            scaleOut(targetScale = 0.9f, animationSpec = tween(340)) +
            fadeOut(animationSpec = tween(350))
        },
    ) {
        val state = NavState(nav::navigate, nav::popBackStack)

        composable<NavRoute.Home> { HomeScreen(onNavigate = nav::navigate) }

        composable<NavRoute.Settings> { SettingsScreen(state) }

        composable<NavRoute.Settings.Location> { LocationSettingsScreen(state) }

        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (BuildConfig.BUILD_TYPE != "release") {
            composable<NavRoute.Settings.Dev> { DevSettingsScreen(state) }

            activity<NavRoute.Settings.Dev.LineList> { activityClass = LineListActivity::class }
            activity<NavRoute.Settings.Dev.DeparturePerformance> { activityClass = DeparturePerformanceActivity::class }
        }

        activity<NavRoute.Search> { activityClass = SearchActivity::class }
        activity<NavRoute.VehicleMap> { activityClass = VehicleMapActivity::class }
        activity<NavRoute.ConnectionSearch> { activityClass = ConnectionSearchActivity::class }
        activity<NavRoute.VehiclesList> { activityClass = VehiclesListActivity::class }
        activity<NavRoute.Diversions> { activityClass = DiversionsActivity::class }
        activity<NavRoute.News> { activityClass = NewsActivity::class }
        activity<NavRoute.About> { activityClass = AboutActivity::class }

        activity<NavRoute.Settings.Departures> { activityClass = DeparturesSettingsActivity::class }
        activity<NavRoute.Settings.Updates> { activityClass = UpdatingSettingsActivity::class }

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
private fun InstallPermissionDialog(onContinue: () -> Unit, onDismiss: () -> Unit) {
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


