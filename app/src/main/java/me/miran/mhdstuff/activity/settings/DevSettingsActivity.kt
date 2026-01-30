package me.miran.mhdstuff.activity.settings

import androidx.compose.runtime.Composable
import me.miran.mhdstuff.activity.base.NavigationActivity
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.devtest.DeparturePerformanceActivity
import me.miran.mhdstuff.activity.devtest.LineListActivity

class DevSettingsActivity : NavigationActivity(R.string.dev_settings){
    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.code, "Line test", LineListActivity::class)
        NavigationItem(R.drawable.code, "Departure perf test", DeparturePerformanceActivity::class)
    }
}