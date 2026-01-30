package me.miran.mhdstuff.activity.settings

import androidx.compose.runtime.Composable
import me.miran.mhdstuff.activity.base.NavigationActivity
import me.miran.mhdstuff.R

class SettingsActivity : NavigationActivity(R.string.settings) {

    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.code, "dev options", DevSettingsActivity::class)
    }


}