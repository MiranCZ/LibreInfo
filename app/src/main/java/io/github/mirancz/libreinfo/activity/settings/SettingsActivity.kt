package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import io.github.mirancz.libreinfo.activity.base.NavigationActivity
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R


class SettingsActivity : NavigationActivity(R.string.settings) {

    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.palette, R.string.departures_settings, DeparturesSettingsActivity::class)

        NavigationItem(R.drawable.location_dot, R.string.location, LocationSettingsActivity::class)

        @Suppress("KotlinConstantConditions")
        if (BuildConfig.AUTO_UPDATE_ENABLED) {
            NavigationItem(R.drawable.download, R.string.updating_settings, UpdatingSettingsActivity::class)
        }

        @Suppress("KotlinConstantConditions")
        if (BuildConfig.DEBUG) {
            NavigationItem(R.drawable.code, "dev options", DevSettingsActivity::class)
        }
    }


}