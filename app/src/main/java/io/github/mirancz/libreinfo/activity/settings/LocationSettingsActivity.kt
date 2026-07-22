package io.github.mirancz.libreinfo.activity.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.component.AppSwitch
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.activity.component.SettingSwitch
import io.github.mirancz.libreinfo.util.PermissionHelper
import io.github.mirancz.libreinfo.util.Settings


class LocationSettingsActivity : KBaseActivity(R.string.location) {

    companion object {
        private const val DISTANCE_SORT_KEY: String = "LOCATION_SORT_STOPS"

        fun shouldSortByDistance() =
            Settings.get().getBoolean(DISTANCE_SORT_KEY, false)

    }

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current
        val activity = this


        var locationEnabled by remember { mutableStateOf(PermissionHelper.locationEnabled(context))}

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { results: Map<String, Boolean> ->
            locationEnabled = results.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) ||
                    results.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)

            val settings = Settings.get()
            val requestedBefore = settings.getBoolean("location_permissions_requested", false)

            Settings.get().putBoolean("location_permissions_requested", true).flush()

            if (!locationEnabled) {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.ACCESS_COARSE_LOCATION
                )

                // likely permanently denied -> need to direct to settings
                if (!showRationale && requestedBefore) {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }
        }

        val launchPermissionDialog = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        Column {
            if (!locationEnabled) {
                Container {
                    Column {
                        Text(
                            stringResource(R.string.allow_location_text),
                            color = colorResource(R.color.secondaryColor)
                        )

                        AppButton(color = colorResource(R.color.light_blue), onClick = {
                            launchPermissionDialog()
                        }) {
                            Text(
                                stringResource(R.string.allow_location),
                                color = colorResource(R.color.secondaryColor)
                            )
                        }
                    }
                }
            }


            if (!locationEnabled) {
                Box(Modifier.consumeClicks().alpha(0.5f)) {
                    SettingsCard()
                }
            } else {
                SettingsCard()
            }
        }

    }

    @Composable
    private fun SettingsCard() {
        Row {
            SettingSwitch(stringResource(R.string.sort_stops_by_distance), DISTANCE_SORT_KEY)
            Divider()
        }
    }


    private fun Modifier.consumeClicks(pass: PointerEventPass = PointerEventPass.Initial) =
        this.then(
            Modifier.pointerInput(pass) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = pass)
                    down.consume()
                    waitForUpOrCancellation(pass)
                }
            }
        )

}