package me.miran.libreinfo.activity.settings

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.IdStorage
import me.miran.libreinfo.parsing.types.StopTime
import me.miran.libreinfo.parsing.types.Time
import me.miran.libreinfo.parsing.types.TimeMark
import me.miran.libreinfo.parsing.types.departure.Departure
import me.miran.libreinfo.parsing.types.departure.DepartureEntry
import me.miran.libreinfo.parsing.types.departure.VehicleInfo
import me.miran.libreinfo.util.DeparturesSettings
import me.miran.libreinfo.util.LocalDeparturesSettings

class DeparturesThemingActivity : KBaseActivity(R.string.departures_theming) {

    class DeparturesSettingsViewModel : ViewModel() {
        var settings by mutableStateOf(DeparturesSettings())
            private set

        fun load() {
            settings = DeparturesSettings.fromPrefs()
        }

        fun update(s: DeparturesSettings) {
            DeparturesSettings.save(s); settings = s
        }
    }

    @Composable
    override fun CreateElements() {
        val vm: DeparturesSettingsViewModel = viewModel()

        var storage: IdStorage? by remember { mutableStateOf(IdStorage.getInstanceOrNull()) }

        LaunchedEffect(Unit) {
            vm.load()

            val storageRes = withContext(Dispatchers.IO) {
                IdStorage.getInstance()
            }

            storage = storageRes

        }
        val settings = vm.settings

        val translationMap = mapOf(
            Pair(DelayRenderType.NONE, stringResource(R.string.do_not_show)),
            Pair(DelayRenderType.PARENTHESES, stringResource(R.string.parentheses)),
            Pair(DelayRenderType.BOX, stringResource(R.string.rectangle)),
        )

        CompositionLocalProvider(LocalDeparturesSettings provides settings) {
            LazyColumn {
                item {
                    Crossfade(targetState = storage) { local ->
                        if (local != null) {
                            PreviewCard("Náhled",local)
                        } else {
                            DepartureEntryShimmer(rememberActivityShimmer(), "Náhled")
                        }
                    }

                }
                item {
                    SettingDropdown(stringResource(R.string.delay_render), settings.delayRender, DelayRenderType.entries, displayString = {
                        translationMap.getOrDefault(it, "??")
                    }) {
                        vm.update(settings.copy(delayRender = it))
                    }
                    Divider()
                }
                item {
                    SettingToggleRow(stringResource(R.string.show_lowfloor), settings.showLowFloor) {
                        vm.update(settings.copy(showLowFloor = it))
                    }
                    Divider()
                }
                item {
                    SettingStepperRow(stringResource(R.string.departure_count_max), settings.maxEntries, 1, 10) {
                        vm.update(settings.copy(maxEntries = it))
                    }
                    Divider()
                }
            }
        }
    }

    @Composable
    private fun PreviewCard(name: String,storage: IdStorage) {
        DeparturePreview(
            name,
            storage,
            PreviewEntry(
                lineId = 1,
                destination = "Rakovecká",
                minutesFromNow = 1,
                delayMinutes = 0,
                certain = true,
                isLowFloor = true
            ),

            PreviewEntry(
                lineId = 67,
                destination = "Lorem ipsum dolor sit amet, consectetur adipiscing elit." +
                        "Vestibulum urna purus, sodales a aliquam et, bibendum ultrices dui." +
                        "Suspendisse bibendum, justo et gravida dapibus, augue mi interdum arcu, sed sollicitudin lacus lorem non lorem",
                minutesFromNow = 15,
                delayMinutes = 0,
                certain = false,
                isLowFloor = false
            ),


            PreviewEntry(
                lineId = 99,
                destination = "Dobrou noc",
                minutesFromNow = 35,
                delayMinutes = 2,
                certain = true,
                isLowFloor = true
            ),

            PreviewEntry(
                lineId = 10,
                destination = "Stránská skála",
                minutesFromNow = 70,
                delayMinutes = 8,
                certain = true,
                isLowFloor = true
            ),

            PreviewEntry(
                lineId = 201,
                destination = "Nějaká vesnice",
                minutesFromNow = 20,
                delayMinutes = 50,
                certain = true,
                isLowFloor = true
            )


        )
    }

    @Composable
    private fun DeparturePreview(name: String, storage: IdStorage, vararg entries: PreviewEntry) {
        val inputEntries = ArrayList<DepartureEntry>()

        for (entry in entries) {
            val stopTime = StopTime(Time.now().addMinutes(entry.minutesFromNow-entry.delayMinutes))
            val timeMark = TimeMark(stopTime, entry.certain, false)

            val vehicleInfo = if (entry.certain) {
                VehicleInfo(0, entry.delayMinutes)
            } else {
                VehicleInfo()
            }

            inputEntries.add(
                DepartureEntry(
                    storage.lineStorage.getAlias(entry.lineId),
                    entry.destination,
                    0,
                    0,
                    entry.isLowFloor,
                    timeMark,
                    0,
                    vehicleInfo
                )
            )
        }

        val dep = Departure(0, name, inputEntries)

        val settings = LocalDeparturesSettings.current

        CompositionLocalProvider(LocalDeparturesSettings provides settings.copy(maxEntries = entries.size)) {
            Departure(dep, post = null)
        }
    }

    data class PreviewEntry(
        val lineId: Int,
        val destination: String,
        val minutesFromNow: Int,
        val delayMinutes: Int,
        val certain: Boolean,
        val isLowFloor: Boolean
    )

    @Composable
    private fun SettingToggleRow(
        label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }

    @Composable
    private fun <T> SettingDropdown(label: String, selected: T, options: List<T>, displayString: (T) -> String = { it.toString() }, onSelect: (T) -> Unit) {
        Row (
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(16.dp))
            AppDropdown(selected, options, onSelect, displayString = displayString)
        }
    }

    @Composable
    private fun SettingStepperRow(
        label: String, value: Int, min: Int, max: Int, onValueChange: (Int) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (value > min) onValueChange(value - 1) }, enabled = value > min
                ) {
                    Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    value.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { if (value < max) onValueChange(value + 1) }, enabled = value < max
                ) {
                    Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
