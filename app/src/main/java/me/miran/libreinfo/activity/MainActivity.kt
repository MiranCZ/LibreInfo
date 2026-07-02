package me.miran.libreinfo.activity

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.BuildConfig
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.NavigationActivity
import me.miran.libreinfo.activity.settings.SettingsActivity
import me.miran.libreinfo.util.ApkInstaller
import me.miran.libreinfo.util.UpdateHelper

class MainActivity : NavigationActivity(R.string.app_name) {


    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.bus_light_full, R.string.departures, SearchActivity::class)
        NavigationItem(R.drawable.location_arrow, R.string.vehicle_map, VehicleMapActivity::class, true)
        NavigationItem(R.drawable.map_regular_full, R.string.connection_search, ConnectionSearchActivity::class, true)
        NavigationItem(R.drawable.list_ul_regular, R.string.vehicles, VehiclesListActivity::class, true)
        NavigationItem(R.drawable.bolt_regular, R.string.events, EventsActivity::class, true)
        NavigationItem(R.drawable.triangle_exclamation_regular, R.string.diversions, DiversionsActivity::class, true)
        NavigationItem(R.drawable.message_lines_regular, R.string.news, NewsActivity::class, true)
//        NavigationItem(R.drawable.address_card_regular, "Šalinkarta") {}
//        NavigationItem(R.drawable.code_fork_regular, R.string.schemes) {}
        NavigationItem(R.drawable.gear_regular, R.string.settings, SettingsActivity::class)
        NavigationItem(R.drawable.circle_info_regular, R.string.about, AboutActivity::class)
    }

    @Composable
    override fun BottomOverlay(modifier: Modifier) {
        if (!BuildConfig.AUTO_UPDATE_ENABLED) return

        val context = LocalContext.current
        var updateReady by rememberSaveable { mutableStateOf(false) }
        var dismissed by rememberSaveable { mutableStateOf(false) }
        var showRationale by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            updateReady = withContext(Dispatchers.IO) {
                UpdateHelper.isUpdateDownloaded(context)
            }
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
                        strokeWidth = 3.dp,
                        color = colorResource(R.color.light_blue)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        stringResource(R.string.update_preparing),
                        color = colorResource(R.color.secondaryColor)
                    )
                }
            }
        }
    }

    @Composable
    private fun UpdateBanner(modifier: Modifier = Modifier, onInstall: () -> Unit, onDismiss: () -> Unit) {
        Container(modifier = modifier.padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.update_ready),
                    color = colorResource(R.color.secondaryColor),
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.Center) {
                    AppButton(
                        modifier = Modifier.fillMaxWidth(0.5f).padding(4.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, colorResource(R.color.secondary_color_tone)),
                        onClick = onDismiss
                    ) {
                        Text(
                            stringResource(R.string.close),
                            color = colorResource(R.color.secondary_color_light_tone)
                        )
                    }

                    AppButton(
                        modifier = Modifier.padding(4.dp),
                        color = colorResource(R.color.light_blue),
                        onClick = onInstall
                    ) {
                        Text(stringResource(R.string.install), color = Color.White)

                    }
                }
            }
        }
    }

    @Composable
    private fun InstallPermissionDialog(onContinue: () -> Unit, onDismiss: () -> Unit) {
        Dialog(onDismissRequest = onDismiss) {
            Container {
                Column {
                    Text(
                        stringResource(R.string.install_permission_title),
                        color = colorResource(R.color.secondaryColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.install_permission_rationale),
                        color = colorResource(R.color.secondaryColor)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = onContinue) {
                            Text(stringResource(R.string.continue_action))
                        }
                    }
                }
            }
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
                        color = colorResource(R.color.secondaryColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.install_failed_message),
                        color = colorResource(R.color.secondaryColor)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.debug_info),
                            color = colorResource(R.color.secondary_color_light_tone),
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colorResource(R.color.secondary_color_light_tone)
                        )
                    }

                    if (expanded) {
                        Spacer(Modifier.height(4.dp))
                        SelectionContainer {
                            Text(
                                detail,
                                color = colorResource(R.color.secondary_color_light_tone),
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

}
