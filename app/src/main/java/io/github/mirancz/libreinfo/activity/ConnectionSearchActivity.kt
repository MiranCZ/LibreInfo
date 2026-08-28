package io.github.mirancz.libreinfo.activity

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.activity.component.AppButton
import io.github.mirancz.libreinfo.activity.component.ConfirmDialog
import io.github.mirancz.libreinfo.activity.component.Container
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs


class ConnectionSearchActivity : KBaseActivity(R.string.connection_search) {


    class ConnectionViewModel : ViewModel() {
        var fromStop by mutableStateOf<Stop?>(null)
        var toStop by mutableStateOf<Stop?>(null)
        var departureDateTime: LocalDateTime? by mutableStateOf(null)
    }

    @Composable
    override fun CreateElements() {
        val vm: ConnectionViewModel = viewModel()
        var showTimePicker by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            StopsPicker(vm)

            Spacer(Modifier.height(8.dp))

            TimePicker(vm) { showTimePicker = true }

            Spacer(Modifier.height(16.dp))

            SearchButton(vm)

            Spacer(Modifier.height(24.dp))

            Container(color = colorResource(R.color.ui_warning)) {
                Text(
                    stringResource(R.string.connection_dev_warning),
                    color = colorResource(R.color.secondaryColor)
                )
            }
        }

        if (showTimePicker) {
            @Suppress("AssignedValueIsNeverRead")
            // linter is just lying here, setting `showTimePicker``DOES have side effects
            TimePickerDialog(
                initialDateTime = vm.departureDateTime ?: LocalDateTime.now(),
                onDismiss = {
                    showTimePicker = false
                },
                onConfirm = { dateTime ->
                    vm.departureDateTime = dateTime
                    showTimePicker = false
                }
            )
        }

    }

    @Composable
    private fun TimePicker(vm: ConnectionViewModel, showDialog: () -> Unit) {
        Box {
            Container(onClick = {
                showDialog()
            }) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = colorResource(R.color.secondary_color_tone),
                        modifier = Modifier
                            .size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        stringResource(R.string.departure) + " ",
                        fontWeight = FontWeight.Medium,
                        color = colorResource(R.color.secondary_color_light_tone),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically
            ) {
                val departureDateTime = vm.departureDateTime
                val timeChosen = departureDateTime != null

                val text = if (timeChosen) {
                    val date = departureDateTime.toLocalDate()
                    val time = departureDateTime.toLocalTime()

                    "${dateLabel(date)} ${time.format(TIME_FORMATTER)}"
                } else {
                    stringResource(R.string.departure_now)
                }
                Text(
                    text,
                    color = colorResource(R.color.secondaryColor),
                    fontWeight = FontWeight.Medium,
                )

                if (timeChosen) {
                    IconButton(
                        onClick = {
                            vm.departureDateTime = null
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = colorResource(R.color.secondaryColor),
                            modifier = Modifier
                                .size(20.dp)
                        )
                    }
                } else {
                    // slightly more to the end so it looks more normal
                    Spacer(Modifier.width((20 + 16).dp))
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
    private fun SearchButton(vm: ConnectionViewModel) {
        val validStops = vm.fromStop != null && vm.toStop != null && vm.fromStop != vm.toStop

        AppButton(
            onClick = {
                if (validStops) {
                    onSearch(vm)
                }
            },
            color = colorResource(R.color.light_blue),
            enabled = validStops,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.search), color = Color.White)
        }
    }

    @Composable
    private fun StopsPicker(vm: ConnectionViewModel) {
        Container(
            innerPadding = 8.dp,
        ) {
            Box {
                Column(Modifier.fillMaxWidth()) {
                    StopField(
                        stop = vm.fromStop,
                        placeholder = stringResource(R.string.from),
                        pos = StopFieldPos.Top,
                        onClick = { launchStopPicker(fromStopPicker) }
                    )
                    Divider()
                    StopField(
                        stop = vm.toStop,
                        placeholder = stringResource(R.string.to),
                        pos = StopFieldPos.Bottom,
                        onClick = { launchStopPicker(toStopPicker) }
                    )
                }

                var rotation by remember { mutableFloatStateOf(0f) }
                val animatedRotation by animateFloatAsState(
                    targetValue = rotation,
                    animationSpec = tween(durationMillis = 300),
                    label = "iconRotation"
                )
                IconButton(
                    onClick = {
                        rotation += 180f
                        val from = vm.fromStop
                        vm.fromStop = vm.toStop
                        vm.toStop = from
                    },
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 6.dp)
                        .background(colorResource(R.color.on_widget_background), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = colorResource(R.color.light_blue),
                        modifier = Modifier
                            .size(32.dp)
                            .rotate(animatedRotation)
                    )
                }
            }

        }
    }

    @Composable
    private fun StopField(
        stop: Stop?,
        placeholder: String,
        pos: StopFieldPos,
        onClick: () -> Unit
    ) {
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
                            .size(20.dp),
                        tint = colorResource(R.color.light_blue)
                    )
                },
            )

            val shape = when (pos) {
                StopFieldPos.Top -> RoundedCornerShape(8.dp).copy(
                    bottomEnd = CornerSize(0),
                    bottomStart = CornerSize(0)
                )

                StopFieldPos.Bottom -> RoundedCornerShape(8.dp).copy(
                    topEnd = CornerSize(0),
                    topStart = CornerSize(0)
                )
            }

            Box(
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .clickable(onClick = onClick)
            )
        }
    }

    @Composable
    private fun TimePickerDialog(
        initialDateTime: LocalDateTime,
        onConfirm: (LocalDateTime) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val today = remember { LocalDate.now() }
        val dates = remember { (-1..14).map { today.plusDays(it.toLong()) } }
        val dateLabels = dates.map { dateLabel(it) }

        var dateIndex by remember { mutableStateOf(dates.indexOf(initialDateTime.toLocalDate()).coerceAtLeast(0)) }
        var hour by remember { mutableStateOf(initialDateTime.hour) }

        // should probably be `ceilDiv` but then the hour would need to be moved in certain cases
        var minuteIndex by remember { mutableStateOf(initialDateTime.minute / 5) }


        ConfirmDialog(
            stringResource(R.string.connection_date_time),
            stringResource(R.string.cancel),
            stringResource(R.string.confirm),
            onDismiss,
            onConfirm = {
                onConfirm(LocalDateTime.of(dates[dateIndex], LocalTime.of(hour, minuteIndex * 5)))
            },
        ) {

            Box(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorResource(R.color.on_widget_background).copy(alpha = 0.6f))
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelPicker(dateLabels, startIndex = 1, modifier = Modifier.weight(2f)) { dateIndex = it }

                    WheelPicker(
                        (0..<24).toList(),
                        Modifier.weight(1f),
                        infinite = true,
                        startIndex = initialDateTime.hour,
                        formatter = { "%02d".format(it) }
                    ) { hour = it }
                    Text(
                        ":",
                        color = colorResource(R.color.secondary_color_light_tone),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )


                    WheelPicker(
                        (0..<60 step 5).toList(),
                        Modifier.weight(1f),
                        infinite = true,
                        startIndex = initialDateTime.minute / 5,
                        formatter = { "%02d".format(it) }
                    ) { minuteIndex = it }
                }
            }
        }
    }

    @Composable
    private fun <T> WheelPicker(
        values: List<T>,
        modifier: Modifier = Modifier,
        infinite: Boolean = false,
        startIndex: Int = 0,
        formatter: (T) -> String = { it.toString() },
        onSelected: (Int) -> Unit = {},
    ) {
        val initialListIndex = if (infinite) {
            Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % values.size)  + startIndex
        } else startIndex

        val listState = rememberLazyListState(initialListIndex)
        val flingBehavior = rememberSnapFlingBehavior(listState)

        // whichever item is closest to center
        val centerIndex by remember {
            derivedStateOf {
                val info = listState.layoutInfo
                val center = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                info.visibleItemsInfo
                    .minByOrNull { abs(it.offset + it.size / 2f - center) }
                    ?.index ?: initialListIndex
            }
        }

        val fadeBrush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.4f to Color.Black,
            0.6f to Color.Black,
            1f to Color.Transparent
        )

        LaunchedEffect(centerIndex) {
            onSelected(centerIndex % values.size)
        }

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = WHEEL_ITEM_HEIGHT * 2),
            modifier = modifier
                .height(WHEEL_ITEM_HEIGHT * 5)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(brush = fadeBrush, blendMode = BlendMode.DstIn)
                }
        ) {
            items(count = if (infinite) Int.MAX_VALUE else values.size) {
                val index = it % values.size

                val selected = it == centerIndex

                val color =
                    if (selected) colorResource(R.color.secondaryColor) else colorResource(R.color.secondary_color_tone)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(WHEEL_ITEM_HEIGHT),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        formatter(values[index]),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 24.sp,
                        color = color
                    )
                }
            }
        }

    }

    private fun onSearch(vm: ConnectionViewModel) {
        val from = vm.fromStop
        val to = vm.toStop
        if (from == null || to == null) {
            showSnackBar(getString(R.string.connection_select_stops), SnackBarType.ERROR)
            return
        }
        val departureTime = vm.departureDateTime ?: LocalDateTime.now()

        val time = departureTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString()
        startActivity(ConnectionResultsActivity::class) { intent ->
            intent.putExtra("fromStop", from)
            intent.putExtra("toStop", to)
            intent.putExtra("departureTime", time)
        }
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

    private enum class StopFieldPos {
        Top, Bottom
    }

    private companion object {

        val WHEEL_ITEM_HEIGHT = 40.dp
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d.M.")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

}