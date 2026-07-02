package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.runtime.Composable
import io.github.mirancz.libreinfo.activity.base.NavigationActivity
import io.github.mirancz.libreinfo.activity.devtest.DeparturePerformanceActivity
import io.github.mirancz.libreinfo.activity.devtest.LineListActivity
import io.github.mirancz.libreinfo.R

class DevSettingsActivity : NavigationActivity(R.string.dev_settings){
    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.code, "Line test", LineListActivity::class)
        NavigationItem(R.drawable.code, "Departure perf test", DeparturePerformanceActivity::class)
    }
}