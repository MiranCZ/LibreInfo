package me.miran.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import me.miran.libreinfo.activity.base.NavigationActivity
import me.miran.libreinfo.R

class SettingsActivity : NavigationActivity(R.string.settings) {

    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.code, "dev options", DevSettingsActivity::class)
    }


}