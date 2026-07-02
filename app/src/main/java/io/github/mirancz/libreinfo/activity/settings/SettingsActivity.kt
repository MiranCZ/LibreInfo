package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import io.github.mirancz.libreinfo.activity.base.NavigationActivity
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R


class SettingsActivity : NavigationActivity(R.string.settings) {

    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.palette, R.string.departures_theming, DeparturesThemingActivity::class)
        if (BuildConfig.DEBUG) {
            NavigationItem(R.drawable.code, "dev options", DevSettingsActivity::class)
        }
    }


}