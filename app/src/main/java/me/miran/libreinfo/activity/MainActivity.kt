package me.miran.libreinfo.activity

import androidx.compose.runtime.Composable
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.NavigationActivity
import me.miran.libreinfo.activity.settings.SettingsActivity

class MainActivity : NavigationActivity(R.string.app_name) {


    @Composable
    override fun CreateNavigation() {
        NavigationItem(R.drawable.bus_light_full, R.string.departures, SearchActivity::class)
        NavigationItem(R.drawable.location_arrow, R.string.vehicle_map, VehicleMapActivity::class, true)
        NavigationItem(R.drawable.map_regular_full, R.string.connection_search, ConnectionSearchActivity::class, true)
        NavigationItem(R.drawable.list_ul_regular, R.string.vehicles, VehiclesListActivity::class, true)
        NavigationItem(R.drawable.bolt_regular, R.string.events, EventsActivity::class, true)
        NavigationItem(R.drawable.triangle_exclamation_regular, R.string.diversions, DiversionsActivity::class, true)
        NavigationItem(R.drawable.message_lines_regular, R.string.news, NewsActivity::class, true)
//        NavigationItem(R.drawable.address_card_regular, "Šalinkarta") {}
//        NavigationItem(R.drawable.code_fork_regular, R.string.schemes) {}
        NavigationItem(R.drawable.gear_regular, R.string.settings, SettingsActivity::class)
        NavigationItem(R.drawable.circle_info_regular, R.string.about, AboutActivity::class)
    }



}