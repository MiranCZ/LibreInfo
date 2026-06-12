package me.miran.libreinfo.activity

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.activity.base.snackbar.SnackBarType
import me.miran.libreinfo.parsing.types.stop.Stop
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
class ConnectionSearchActivity : KBaseActivity(R.string.connection_search) {

    class ConnectionViewModel : ViewModel() {
        var fromStop by mutableStateOf<Stop?>(null)
        var toStop by mutableStateOf<Stop?>(null)
        var departureDate: LocalDate by mutableStateOf(LocalDate.now())
        var departureTime: LocalTime by mutableStateOf(LocalTime.now())
    }

    private val fromStopPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            ViewModelProvider(this)[ConnectionViewModel::class.java].fromStop =
                result.data?.getParcelableExtra(SearchActivity.EXTRA_RESULT_STOP)
        }
    }

    private val toStopPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            ViewModelProvider(this)[ConnectionViewModel::class.java].toStop =
                result.data?.getParcelableExtra(SearchActivity.EXTRA_RESULT_STOP)
        }
    }

    private fun launchStopPicker(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(this, SearchActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_PICKER_MODE, true)
        }
        launcher.launch(intent)
        overridePendingTransition(R.anim.fast_scale_up, R.anim.fast_fade_out)
    }

    @Composable
    override fun CreateElements() {
        val vm: ConnectionViewModel = viewModel()

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        val dateFormatter = remember { DateTimeFormatter.ofPattern("d. M. yyyy") }
        val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            StopField(
                stop = vm.fromStop,
                placeholder = stringResource(R.string.from),
                onClick = { launchStopPicker(fromStopPicker) }
            )

            Spacer(Modifier.height(8.dp))

            StopField(
                stop = vm.toStop,
                placeholder = stringResource(R.string.to),
                onClick = { launchStopPicker(toStopPicker) }
            )

            Spacer(Modifier.height(16.dp))

            Container(
                onClick = { showDatePicker = true },
                innerPadding = 16.dp
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.departure_date),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        vm.departureDate.format(dateFormatter),
                        color = colorResource(R.color.light_blue)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Container(
                onClick = { showTimePicker = true },
                innerPadding = 16.dp
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(R.string.departure_time),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        vm.departureTime.format(timeFormatter),
                        color = colorResource(R.color.light_blue)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            AppButton(
                onClick = {
                    val from = vm.fromStop
                    val to = vm.toStop
                    if (from == null || to == null) {
                        showSnackBar("Vyberte počáteční i cílovou zastávku", SnackBarType.ERROR)
                        return@AppButton
                    }
                    val time = LocalDateTime.of(vm.departureDate, vm.departureTime).toString()
                    startActivity(ConnectionResultsActivity::class) { intent ->
                        intent.putExtra("fromStop", from)
                        intent.putExtra("toStop", to)
                        intent.putExtra("departureTime", time)
                    }
                },
                color = colorResource(R.color.light_blue)
            ) {
                Text(stringResource(R.string.search), color = Color.White)
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = vm.departureDate
                    .atStartOfDay(ZoneId.of("UTC"))
                    .toInstant()
                    .toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            vm.departureDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }) {
                        Text("Potvrdit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Zrušit")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = vm.departureTime.hour,
                initialMinute = vm.departureTime.minute,
                is24Hour = true
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        vm.departureTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) {
                        Text("Potvrdit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Zrušit")
                    }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
    }

    @Composable
    private fun StopField(stop: Stop?, placeholder: String, onClick: () -> Unit) {
        Box {
            AppTextField(
                value = stop?.name ?: "",
                placeHolder = placeholder,
                readOnly = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.stop),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(20.dp),
                        tint = colorResource(R.color.light_blue)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colorResource(R.color.light_blue)
                    )
                }
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ) { onClick() }
            )
        }
    }
}
