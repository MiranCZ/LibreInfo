package io.github.mirancz.libreinfo.activity.settings

import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import io.github.mirancz.libreinfo.activity.InstallPermissionDialog
import io.github.mirancz.libreinfo.util.ApkInstaller
import io.github.mirancz.libreinfo.util.AppUpdater
import io.github.mirancz.libreinfo.util.UpdateDownloader
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.ui.LocalSnackbarHostState
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.AppButton
import io.github.mirancz.libreinfo.ui.components.AppSwitch
import io.github.mirancz.libreinfo.ui.components.ConfirmDialog
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.ui.components.Divider
import io.github.mirancz.libreinfo.ui.components.PrimaryTextButton
import io.github.mirancz.libreinfo.ui.components.SecondaryTextButton
import io.github.mirancz.libreinfo.ui.show
import io.github.mirancz.libreinfo.ui.showError
import io.github.mirancz.libreinfo.ui.showInfo

@Composable
fun UpdatingSettingsScreen(state: NavState) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    var autoUpdate by remember { mutableStateOf(AppUpdater.isAutoUpdateEnabled()) }
    var lastCheck by remember { mutableLongStateOf(AppUpdater.getLastCheckMillis()) }
    var checking by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var pendingUpdate by remember { mutableStateOf<UpdateDownloader.CheckResult?>(null) }
    var showRationale by remember { mutableStateOf(false) }

    // Returning from the "install unknown apps" settings screen; continue into the installation if it was granted
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (ApkInstaller.installAllowed(context)) {
            showRationale = false
            ApkInstaller.launchInstall(context)
        }
    }

    ScreenScaffold(stringResource(R.string.updating_settings), onBack = state.onBack) {
        Column {
            AutoUpdateRow(autoUpdate) {
                autoUpdate = it
                AppUpdater.setAutoUpdateEnabled(context, it)
            }
            Divider()

            LastCheckedRow(lastCheck)

            val upToDateText = stringResource(R.string.update_check_up_to_date)
            val failedText = stringResource(R.string.update_check_failed)


            CheckButton(checking) {
                if (checking) return@CheckButton
                checking = true


                scope.launch {
                    val check = withContext(Dispatchers.IO) {
                        UpdateDownloader.checkForUpdate(context)
                    }
                    lastCheck = AppUpdater.getLastCheckMillis()
                    checking = false

                    when (check.status) {
                        UpdateDownloader.CheckStatus.UPDATE_AVAILABLE -> pendingUpdate = check

                        UpdateDownloader.CheckStatus.UP_TO_DATE -> snackbar.showInfo(upToDateText)

                        else -> snackbar.showError(failedText)
                    }
                }
            }
        }

        val updateFailedText = stringResource(R.string.update_download_failed)

        val pending = pendingUpdate
        if (pending != null) {
            DownloadPromptDialog(
                versionName = pending.versionName,
                downloading = downloading,
                onDownload = {
                    if (!downloading) {
                        downloading = true
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                UpdateDownloader.download(context, pending)
                            }
                            downloading = false
                            pendingUpdate = null

                            if (result != UpdateDownloader.UpdateResult.DOWNLOADED) {
                                snackbar.show(updateFailedText, SnackBarType.ERROR)
                                return@launch
                            }

                            if (ApkInstaller.installAllowed(context)) {
                                ApkInstaller.launchInstall(context)
                            } else {
                                showRationale = true
                            }
                        }
                    }
                },
                onDismiss = { pendingUpdate = null })
        }

        if (showRationale) {
            InstallPermissionDialog(
                onContinue = { settingsLauncher.launch(ApkInstaller.unknownSourcesIntent(context)) },
                onDismiss = { showRationale = false }
            )
        }
    }

}



@Composable
private fun AutoUpdateRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                stringResource(R.string.auto_download_updates),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.update_wifi_charging_note),
                fontSize = 13.sp,
                color = MaterialTheme.extendedColors.onSurfaceMedium
            )
        }
        AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LastCheckedRow(lastCheck: Long) {
    val value = if (lastCheck <= 0L) {
        stringResource(R.string.never)
    } else {
        DateUtils.getRelativeTimeSpanString(
            lastCheck, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.last_checked),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            value, fontSize = 14.sp, color = MaterialTheme.extendedColors.onSurfaceMedium
        )
    }
}

@Composable
private fun CheckButton(checking: Boolean, onClick: () -> Unit) {
    AppButton(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        onClick = onClick
    ) {
        if (checking) {
            CircularProgressIndicator(
                Modifier.size(20.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.checking_updates))
        } else {
            Text(stringResource(R.string.check_for_updates))
        }
    }
}

@Composable
private fun DownloadPromptDialog(
    versionName: String, downloading: Boolean, onDownload: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!downloading) onDismiss() }, properties = DialogProperties(
            dismissOnBackPress = !downloading, dismissOnClickOutside = !downloading
        )
    ) {
        Container {
            Column {
                Text(
                    stringResource(R.string.update_available_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(12.dp))

                // to make version name bold
                val template = stringResource(R.string.update_available_message, "\u0000")
                val (before, after) = template.split("\u0000")
                Text(buildAnnotatedString {
                    append(before)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(versionName) }
                    append(after)
                })

                Spacer(Modifier.height(16.dp))

                if (downloading) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.update_downloading))
                    }
                } else {
                    Row(Modifier.fillMaxWidth()) {
                        SecondaryTextButton(stringResource(R.string.cancel), onClick = onDismiss, modifier = Modifier.weight(1f))

                        Spacer(Modifier.width(16.dp))

                        PrimaryTextButton(stringResource(R.string.update_download_action), onClick = onDownload, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}