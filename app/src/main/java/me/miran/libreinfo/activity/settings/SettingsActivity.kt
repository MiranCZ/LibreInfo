package me.miran.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import me.miran.libreinfo.BuildConfig
import me.miran.libreinfo.activity.base.NavigationActivity
import me.miran.libreinfo.R


class SettingsActivity : NavigationActivity(R.string.settings) {

    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.palette, R.string.departures_theming, DeparturesThemingActivity::class)
        if (BuildConfig.DEBUG) {
            NavigationItem(R.drawable.code, "dev options", DevSettingsActivity::class)
        }
    }


}