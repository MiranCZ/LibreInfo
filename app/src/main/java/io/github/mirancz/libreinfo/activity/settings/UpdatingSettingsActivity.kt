package io.github.mirancz.libreinfo.activity.settings

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.util.AppUpdater
import io.github.mirancz.libreinfo.util.UpdateDownloader
import io.github.mirancz.libreinfo.R

class UpdatingSettingsActivity : KBaseActivity(R.string.updating_settings) {

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        var autoUpdate by remember { mutableStateOf(AppUpdater.isAutoUpdateEnabled()) }
        var lastCheck by remember { mutableLongStateOf(AppUpdater.getLastCheckMillis()) }
        var checking by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(false) }
        var pendingUpdate by remember { mutableStateOf<UpdateDownloader.CheckResult?>(null) }

        Column {
            AutoUpdateRow(autoUpdate) {
                autoUpdate = it
                AppUpdater.setAutoUpdateEnabled(context, it)
            }
            Divider()

            LastCheckedRow(lastCheck)

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

                        UpdateDownloader.CheckStatus.UP_TO_DATE -> showSnackBar(
                            getString(R.string.update_check_up_to_date), SnackBarType.INFO
                        )

                        else -> showSnackBar(
                            getString(R.string.update_check_failed), SnackBarType.ERROR
                        )
                    }
                }
            }
        }

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

                            val (message, type) = when (result) {
                                UpdateDownloader.UpdateResult.DOWNLOADED -> getString(R.string.update_check_downloaded) to SnackBarType.SUCCESS

                                else -> getString(R.string.update_download_failed) to SnackBarType.ERROR
                            }
                            showSnackBar(message, type)
                        }
                    }
                },
                onDismiss = { pendingUpdate = null })
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
                    color = colorResource(R.color.secondary_color_light_tone)
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
                value, fontSize = 14.sp, color = colorResource(R.color.secondary_color_light_tone)
            )
        }
    }

    @Composable
    private fun CheckButton(checking: Boolean, onClick: () -> Unit) {
        AppButton(
            modifier = Modifier.padding(16.dp),
            color = colorResource(R.color.light_blue),
            onClick = onClick
        ) {
            if (checking) {
                CircularProgressIndicator(
                    Modifier.size(20.dp), strokeWidth = 2.5.dp, color = Color.White
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.checking_updates), color = Color.White)
            } else {
                Text(stringResource(R.string.check_for_updates), color = Color.White)
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
                        color = colorResource(R.color.secondaryColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.update_available_message, versionName), color = colorResource(R.color.secondaryColor)
                    )

                    Spacer(Modifier.height(16.dp))

                    if (downloading) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = colorResource(R.color.light_blue)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                stringResource(R.string.update_downloading),
                                color = colorResource(R.color.secondaryColor)
                            )
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(R.string.cancel))
                            }
                            TextButton(onClick = onDownload) {
                                Text(stringResource(R.string.update_download_action))
                            }
                        }
                    }
                }
            }
        }
    }
}
