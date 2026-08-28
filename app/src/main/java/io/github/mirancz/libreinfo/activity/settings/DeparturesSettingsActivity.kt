package io.github.mirancz.libreinfo.activity.settings

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.util.AppSettings
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.StopTime
import io.github.mirancz.libreinfo.parsing.types.Time
import io.github.mirancz.libreinfo.parsing.types.TimeMark
import io.github.mirancz.libreinfo.parsing.types.departure.Departure
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureEntry
import io.github.mirancz.libreinfo.parsing.types.departure.VehicleInfo
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.component.AppSwitch
import io.github.mirancz.libreinfo.activity.component.ConfirmDialog
import io.github.mirancz.libreinfo.activity.component.Container

class DeparturesSettingsActivity : KBaseActivity(R.string.departures_settings) {

    class DeparturesSettingsViewModel : ViewModel() {
        var settings by mutableStateOf(DeparturesSettings())
            private set

        // read eagerly so the dropdown never flashes the default before load() lands
        var source by mutableStateOf(AppSettings.Departures.source)
            private set

        fun load() {
            settings = DeparturesSettings.fromPrefs()
            source = AppSettings.Departures.source
        }

        fun update(s: DeparturesSettings) {
            DeparturesSettings.save(s); settings = s
        }

        fun updateSource(s: DepartureSource) {
            AppSettings.Departures.source = s; source = s
        }
    }

    @Composable
    override fun CreateElements() {
        val vm: DeparturesSettingsViewModel = viewModel()

        val provider = AppContainer.storageProvider
        var storage: IdStorage? by remember { mutableStateOf(provider.getInstanceOrNull()) }

        LaunchedEffect(Unit) {
            vm.load()

            val storageRes = withContext(Dispatchers.IO) {
                provider.getInstance()
            }

            storage = storageRes

        }
        val settings = vm.settings

        val translationMap = mapOf(
            Pair(DelayRenderType.NONE, stringResource(R.string.do_not_show)),
            Pair(DelayRenderType.PARENTHESES, stringResource(R.string.parentheses)),
            Pair(DelayRenderType.BOX, stringResource(R.string.rectangle)),
        )

        val sourceTranslationMap = mapOf(
            Pair(DepartureSource.SERVER, stringResource(R.string.departure_source_server)),
            Pair(DepartureSource.LOCAL, stringResource(R.string.departure_source_local)),
        )

        var pendingSource by remember { mutableStateOf<DepartureSource?>(null) }

        pendingSource?.let { pending ->
            LocalSourceWarningDialog(
                onConfirm = { vm.updateSource(pending); pendingSource = null },
                onDismiss = { pendingSource = null }
            )
        }

        CompositionLocalProvider(LocalDeparturesSettings provides settings) {
            LazyColumn {
                item {
                    SettingDropdown(
                        stringResource(R.string.departure_source),
                        vm.source,
                        DepartureSource.entries,
                        displayString = { sourceTranslationMap.getOrDefault(it, "??") }
                    ) {
                        // computing locally yields unofficial data, so that direction needs a confirmation
                        if (it == DepartureSource.LOCAL) pendingSource = it else vm.updateSource(it)
                    }
                    Divider()
                }
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

    /**
     * Warns that on-device departures are unofficial. The confirm button unlocks only after
     * [CONFIRM_COOLDOWN_SECONDS] so the warning is not dismissed reflexively.
     */
    @Composable
    private fun LocalSourceWarningDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        var remaining by remember { mutableIntStateOf(CONFIRM_COOLDOWN_SECONDS) }

        LaunchedEffect(Unit) {
            while (remaining > 0) {
                delay(1000)
                remaining--
            }
        }

        Dialog(onDismissRequest = onDismiss) {
            Container {
                Column {
                    Text(
                        stringResource(R.string.departure_source_warning_title),
                        color = colorResource(R.color.secondaryColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        stringResource(R.string.departure_source_warning_message),
                        color = colorResource(R.color.secondaryColor)
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = onConfirm, enabled = remaining == 0) {
                            val confirm = stringResource(R.string.departure_source_warning_confirm)

                            Text(if (remaining > 0) "$confirm ($remaining)" else confirm)
                        }
                    }
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

    companion object {
        private const val CONFIRM_COOLDOWN_SECONDS = 3
    }

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
