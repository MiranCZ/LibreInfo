package me.miran.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import me.miran.libreinfo.activity.base.NavigationActivity
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.devtest.DeparturePerformanceActivity
import me.miran.libreinfo.activity.devtest.LineListActivity

class DevSettingsActivity : NavigationActivity(R.string.dev_settings){
    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.code, "Line test", LineListActivity::class)
        NavigationItem(R.drawable.code, "Departure perf test", DeparturePerformanceActivity::class)
    }
}