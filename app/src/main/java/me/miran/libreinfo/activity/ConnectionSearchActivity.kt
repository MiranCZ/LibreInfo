package me.miran.libreinfo.activity

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.activity.base.snackbar.SnackBarType
import me.miran.libreinfo.parsing.types.stop.Stop
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class ConnectionSearchActivity : KBaseActivity(R.string.connection_search) {

    class ConnectionViewModel : ViewModel() {
        var fromStop by mutableStateOf<Stop?>(null)
        var toStop by mutableStateOf<Stop?>(null)
        var departureDate: LocalDate by mutableStateOf(LocalDate.now())
        var departureTime: LocalTime by mutableStateOf(LocalTime.now())
    }

    private fun stopPicker(assign: (Stop?) -> Unit) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                assign(result.data?.getParcelableExtra(SearchActivity.EXTRA_RESULT_STOP))
            }
        }

    private val fromStopPicker = stopPicker { stop ->
        ViewModelProvider(this)[ConnectionViewModel::class.java].fromStop = stop
    }

    private val toStopPicker = stopPicker { stop ->
        ViewModelProvider(this)[ConnectionViewModel::class.java].toStop = stop
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
        var showDeparturePicker by remember { mutableStateOf(false) }

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

            // TODO add departure options later
//            Spacer(Modifier.height(8.dp))
//
//            DepartureField(
//                date = vm.departureDate,
//                time = vm.departureTime,
//                onClick = { showDeparturePicker = true }
//            )

            Spacer(Modifier.height(16.dp))

            AppButton(
                onClick = { onSearch(vm) },
                color = colorResource(R.color.light_blue)
            ) {
                Text(stringResource(R.string.search), color = Color.White)
            }

            Spacer(Modifier.height(24.dp))

            Container(color = colorResource(R.color.ui_warning)) {
                Text(
                    stringResource(R.string.connection_dev_warning),
                    color = colorResource(R.color.secondaryColor)
                )
            }
        }

        if (showDeparturePicker) {
            DeparturePickerDialog(
                initialDate = vm.departureDate,
                initialTime = vm.departureTime,
                onDismiss = { showDeparturePicker = false },
                onConfirm = { date, time ->
                    vm.departureDate = date
                    vm.departureTime = time
                    showDeparturePicker = false
                }
            )
        }
    }

    private fun onSearch(vm: ConnectionViewModel) {
        val from = vm.fromStop
        val to = vm.toStop
        if (from == null || to == null) {
            showSnackBar(getString(R.string.connection_select_stops), SnackBarType.ERROR)
            return
        }
        val time = LocalDateTime.of(vm.departureDate, vm.departureTime).toString()
        startActivity(ConnectionResultsActivity::class) { intent ->
            intent.putExtra("fromStop", from)
            intent.putExtra("toStop", to)
            intent.putExtra("departureTime", time)
        }
    }

    @Composable
    private fun StopField(stop: Stop?, placeholder: String, onClick: () -> Unit) {
        TappableField(onClick) {
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
        }
    }

    @Composable
    private fun DepartureField(date: LocalDate, time: LocalTime, onClick: () -> Unit) {
        Container(onClick = onClick) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.departure),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${dateLabel(date)} ${time.format(TIME_FORMATTER)}",
                    color = colorResource(R.color.light_blue),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @Composable
    private fun DeparturePickerDialog(
        initialDate: LocalDate,
        initialTime: LocalTime,
        onConfirm: (LocalDate, LocalTime) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val today = remember { LocalDate.now() }
        val dates = remember { (-1..14).map { today.plusDays(it.toLong()) } }
        val dateLabels = dates.map { dateLabel(it) }
        val hourLabels = remember { (0..23).map { "%02d".format(it) } }
        val minuteLabels = remember { (0..59).map { "%02d".format(it) } }

        var dateIndex by remember { mutableStateOf(dates.indexOf(initialDate).coerceAtLeast(0)) }
        var hour by remember { mutableStateOf(initialTime.hour) }
        var minute by remember { mutableStateOf(initialTime.minute) }

        Dialog(onDismissRequest = onDismiss) {
            Container {
                Column {
                    Text(
                        stringResource(R.string.departure),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(Modifier.fillMaxWidth()) {
                        Box(
                            Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .height(WHEEL_ITEM_HEIGHT)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorResource(R.color.light_gray).copy(alpha = 0.4f))
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            WheelPicker(dateLabels, dateIndex, { dateIndex = it }, Modifier.weight(2f))
                            WheelPicker(hourLabels, hour, { hour = it }, Modifier.weight(1f))
                            Text(":", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            WheelPicker(minuteLabels, minute, { minute = it }, Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            onConfirm(dates[dateIndex], LocalTime.of(hour, minute))
                        }) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WheelPicker(
        labels: List<String>,
        selectedIndex: Int,
        onSelected: (Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

        val centerIndex by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val center = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                info.visibleItemsInfo
                    .minByOrNull { abs(it.offset + it.size / 2f - center) }
                    ?.index ?: selectedIndex
            }
        }

        LaunchedEffect(listState.isScrollInProgress) {
            if (!listState.isScrollInProgress) onSelected(centerIndex)
        }

        Box(modifier.height(WHEEL_ITEM_HEIGHT * 3), contentAlignment = Alignment.Center) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = WHEEL_ITEM_HEIGHT),
                modifier = Modifier.fillMaxHeight()
            ) {
                itemsIndexed(labels) { index, label ->
                    val selected = index == centerIndex
                    Box(
                        Modifier
                            .height(WHEEL_ITEM_HEIGHT)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) colorResource(R.color.light_blue)
                            else colorResource(R.color.secondary_color_tone),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = if (selected) 18.sp else 15.sp,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun dateLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today.minusDays(1) -> stringResource(R.string.date_yesterday)
            today -> stringResource(R.string.date_today)
            today.plusDays(1) -> stringResource(R.string.date_tomorrow)
            else -> date.format(DATE_FORMATTER)
        }
    }

    @Composable
    private fun TappableField(onClick: () -> Unit, content: @Composable () -> Unit) {
        Box {
            content()
            Box(
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
            )
        }
    }

    private companion object {
        val WHEEL_ITEM_HEIGHT = 40.dp
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d.M.")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
