package io.github.mirancz.libreinfo.activity

import android.content.Context
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.CalendarStorage
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.LineAlias
import io.github.mirancz.libreinfo.parsing.types.RouteStop
import io.github.mirancz.libreinfo.parsing.types.StopTime
import io.github.mirancz.libreinfo.parsing.types.Time
import io.github.mirancz.libreinfo.parsing.types.Trip
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.util.DelayUtil
import io.github.mirancz.libreinfo.util.request.RequestHelper
import com.valentinilk.shimmer.Shimmer
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.activity.settings.DelayRenderType
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.VehicleInfo
import io.github.mirancz.libreinfo.util.DelayUtil.getDelayColor
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.load.rememberLoad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.function.Function
import kotlin.time.Duration.Companion.milliseconds

class TripDetailActivity : KBaseActivity(R.string.trip) {

    private var departuresSettings by mutableStateOf(DeparturesSettings())

    override fun onResume() {
        super.onResume()
        departuresSettings = DeparturesSettings.fromPrefs()
    }

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current
        val tripId = intent.getIntExtra("tripId", -1)
        val highlightedStopId = intent.getIntExtra("stopId", -1)
        val vehicleId = intent.getIntExtra("vehicleId", -1)

        val provider = AppContainer.storageProvider

        var storage: IdStorage? by remember { mutableStateOf(provider.getInstanceOrNull()) }
        var tripData: TripInfoData? by remember { mutableStateOf(null) }

        val loadResult = rememberLoad {
            val storage = provider.getInstance()
            val tripInfoData = loadAndParseTripInfoData(storage, tripId, context, highlightedStopId, vehicleId)

            Pair(
                storage, tripInfoData
            )
        }


        AsyncContent(loadResult, loading = { TripDetailShimmer() }) { res ->
            val storage = res.first
            var data by remember{ mutableStateOf(res.second) }
            var lastUpdated: Long? by remember { mutableStateOf(null) }

            // FIXME don't update on finished routes
            if (data.vehicleInfo != VehicleInfo.NONE) {
                lastUpdated = System.currentTimeMillis()

                val lifecycleOwner = LocalLifecycleOwner.current
                LaunchedEffect(Unit) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        while (true) {
                            delay(15_000.milliseconds)

                            withContext(Dispatchers.IO) {
                                data = loadAndParseTripInfoData(
                                    storage,
                                    tripId,
                                    context,
                                    highlightedStopId,
                                    vehicleId
                                )

                                lastUpdated = System.currentTimeMillis()
                            }
                        }
                    }
                }
            }

            CompositionLocalProvider(LocalDeparturesSettings provides departuresSettings) {
                TripInfo(storage, data, lastUpdated)
            }
        }
    }

    private fun loadAndParseTripInfoData(
        storage: IdStorage,
        tripId: Int,
        context: Context,
        highlightedStopId: Int,
        vehicleId: Int
    ): TripInfoData {
        val res = storage.apiStorage.getLineIdAndRoute(tripId)

        var vehicleInfo = VehicleInfo.NONE
        try {
            vehicleInfo =
                RequestHelper.getVehicleInfo(context, res.left!!, res.right!!).map(storage)

        } catch (e: RequestException) {
            showErrorSnackBar(e)
        }


        val trip = storage.tripStorage.trips[tripId]
        var headsign = storage.tripStorage.getTripHeadsign(trip)

        var routeInfoText = getString(R.string.trip_number, res.left, res.right)


        var stops = trip.getRouteStops(storage.routeStopStorage)

        if (trip.blockId.toInt() != -1) {
            val neighbors: MutableList<Trip> =
                ArrayList(storage.tripStorage.getTripsForBlock(trip.blockId.toInt()))

            neighbors.removeIf { t: Trip? ->
                !storage.calendarStorage.available(
                    CalendarStorage.Date.now(), t!!.serviceId.toInt()
                )
            }

            neighbors.sortWith(Comparator.comparing(Function { t: Trip? ->
                storage.routeStopStorage.getRouteStop(
                    t!!.startPos
                ).departure()
            }))

            headsign = storage.tripStorage.getHeadsignForTripList(neighbors, storage)

            routeInfoText = getString(R.string.trip) + " "


            val stopsList: MutableList<RouteStop?> = ArrayList()

            for (i in neighbors.indices) {
                val neighbor = neighbors[i]
                val info = storage.apiStorage.getLineIdAndRoute(neighbor.id)

                if (i != 0) {
                    routeInfoText += " => "
                }
                routeInfoText += info.left.toString() + "/" + info.right

                val routeStops = neighbor.getRouteStops(storage.routeStopStorage)
                for (j in routeStops.indices) {
                    val routeStop = routeStops[j]

                    if (i != 0 && j == 0 && stopsList[stopsList.size - 1]!!.stopId == routeStop.stopId) continue
                    stopsList.add(routeStop)
                }
            }

            stops = stopsList.toTypedArray<RouteStop?>()
        }

        val lineRoute = storage.apiStorage.getLineIdAndRoute(tripId)

        val tripInfoData = TripInfoData(
            tripId,
            highlightedStopId,
            lineRoute.left,
            lineRoute.right,
            vehicleId,
            vehicleInfo,
            headsign,
            routeInfoText,
            stops
        )
        return tripInfoData
    }


    @Composable
    private fun TripDetailShimmer() {
        val shimmer = rememberActivityShimmer()

        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShimmerLineIcon(shimmer)
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        ShimmerText(shimmer, widthFraction = 0.6f, variance = 0.15f, height = 20.dp)
                    }
                }

                Spacer(Modifier.height(2.dp))
                ShimmerText(shimmer, widthFraction = 0.2f, variance = 0.1f, height = 12.dp)
                Spacer(Modifier.height(2.dp))
                ShimmerText(shimmer, widthFraction = 0.35f, variance = 0.1f, height = 12.dp)
                Spacer(Modifier.height(4.dp))

                repeat(20) {
                    StopRowShimmer(shimmer)
                }
            }
        }
    }

    @Composable
    private fun StopRowShimmer(shimmer: Shimmer) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(Modifier.size(20.dp), shimmer, shape = CircleShape)
            Box(Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 4.dp)) {
                ShimmerText(shimmer, widthFraction = 0.7f, variance = 0.2f)
            }
            ShimmerBox(Modifier
                .width(50.dp)
                .height(14.dp), shimmer)
        }
    }

    @Composable
    private fun TripInfo(storage: IdStorage, data: TripInfoData, lastUpdate: Long? = null) {
        val context = LocalContext.current
        val delay = data.vehicleInfo.delay

        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LineIcon(line = storage.lineStorage.getAlias(data.lineId), padding = 0.dp)
                    Text(
                        data.headsign,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(Modifier.fillMaxWidth()) {
                    if (data.vehicleId != -1) {
                        Text(
                            getString(R.string.vehicle_number, data.vehicleId),
                            color = colorResource(R.color.secondary_color_tone),
                            fontSize = 14.sp
                        )
                    }

                    if (delay != -1) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            DelayUtil.getDelayText(context, delay),
                            color = Color(getDelayColor(delay)),
                            fontSize = 14.sp
                        )
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    Text(
                        data.routeInfoText,
                        color = colorResource(R.color.secondary_color_tone),
                        fontSize = 14.sp
                    )

                    if (lastUpdate != null) {
                        Spacer(modifier = Modifier.weight(1f))

                        var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
                        val lifecycleOwner = LocalLifecycleOwner.current

                        LaunchedEffect(Unit) {
                            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                while (true) {
                                    now = System.currentTimeMillis()
                                    delay(1000.milliseconds)
                                }
                            }
                        }

                        val text = "${stringResource(R.string.last_updated)} ${(now - lastUpdate)/1000}s"
                        Text(
                            text,
                            color = colorResource(R.color.secondary_color_tone),
                            fontSize = 14.sp
                        )
                    }
                }

                StopsList(storage, data)
            }
        }

    }


    private data class StopRenderState(
        val index: Int,
        val stopId: Int,
        val alreadyMet: Boolean,
        val leavingStop: Boolean,
        val isTransitionStop: Boolean,
        val isFirst: Boolean,
        val isLast: Boolean,
        val currentDelay: Int,
    )

    private fun stopRenderStates(data: TripInfoData, nowMs: Long): List<StopRenderState> {
        val leavingStopWindowMs = 20_000L

        val vehicleInfo = data.vehicleInfo
        var delay = vehicleInfo.delay
        var alreadyMet = true

        return data.stops.mapIndexed { i, stop ->
            val stopId = stop.stopId.toInt()

            if (vehicleInfo == VehicleInfo.NONE && !stop.departure().isBefore(Time.now())) {
                alreadyMet = false
            }

            val leavingStop =
                stop.equals(vehicleInfo.lastStop) && (nowMs - vehicleInfo.lastModifiedAt) < leavingStopWindowMs

            val isTransitionStop =
                i != 0 && data.stops[i - 1].equals(vehicleInfo.lastStop) && !leavingStop

            val currentDelay = if (alreadyMet) {
                // TODO fix vehicles waiting at stops in the API
                // FIXME multiple stops
                vehicleInfo.previousStops.firstOrNull { stop.equals(it.stop) }?.delay ?: -1
            } else {
                delay
            }
            stop.setDelay(currentDelay)

            val renderState = StopRenderState(
                index = i,
                stopId = stopId,
                alreadyMet = alreadyMet,
                leavingStop = leavingStop,
                isTransitionStop = isTransitionStop,
                isFirst = i == 0,
                isLast = i == data.stops.lastIndex,
                currentDelay = currentDelay,
            )

            if (currentDelay > 0 && !alreadyMet) {
                delay = stop.stopTime.getLoweredDelay()
            }
            if (stop.equals(vehicleInfo.lastStop)) {
                alreadyMet = false
            }

            renderState
        }
    }

    @Composable
    private fun StopsList(storage: IdStorage, data: TripInfoData) {
        val alias: LineAlias = storage.lineStorage.getAlias(data.lineId)
        val renderStates = stopRenderStates(data, nowMs = System.currentTimeMillis())

        val highlightedStopId = intent.getIntExtra("stopId", -1)

        val highlightedIndex = remember(renderStates,highlightedStopId) {
            renderStates.indexOfFirst { it.stopId == highlightedStopId }
                .takeIf { it >= 0 }
        }

        val listState = rememberLazyListState()

        LaunchedEffect(highlightedIndex) {
            val index = highlightedIndex ?: return@LaunchedEffect
            // wait until the list has been measured at least once
            snapshotFlow { listState.layoutInfo.viewportSize.height }
                .first { it > 0 }
                .let { viewportHeight ->
                    listState.scrollToItem(index, -viewportHeight / 4)
                }
        }

        LazyColumn(state = listState) {
            items(renderStates) { state ->
                StopRow(storage = storage, alias = alias, data = data, state = state)
            }
        }
    }

    @Composable
    private fun StopRow(
        storage: IdStorage,
        alias: LineAlias,
        data: TripInfoData,
        state: StopRenderState,
    ) {
        val stop = data.stops[state.index]
        val stopName = storage.stopStorage.getStop(StopId.internal(state.stopId)).name
        val isHighlighted = state.stopId == data.highlightedStopId

        val highlightedIconAlpha = 1f
        val pastIconAlpha = 0.25f
        val futureIconAlpha = 0.5f
        val pastTextAlpha = 0.4f
        val pastLineAlpha = 0.15f
        val defaultLineAlpha = 0.3f

        val iconAlpha = when {
            state.leavingStop -> rememberPulsingAlpha()
            isHighlighted -> highlightedIconAlpha
            state.alreadyMet -> pastIconAlpha
            else -> futureIconAlpha
        }

        val pastAndStationary = state.alreadyMet && !state.leavingStop
        val textAlpha = if (pastAndStationary) pastTextAlpha else 1f
        val lineAlpha = if (pastAndStationary) pastLineAlpha else defaultLineAlpha

        val backgroundColor = Color(alias.backgroundColor)
        val textColor = Color(alias.textColor)

        Row(
            Modifier
                .fillMaxWidth()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawBehind {
                    drawStopConnector(
                        backgroundColor = backgroundColor,
                        textColor = textColor,
                        isTransition = state.isTransitionStop,
                        isFirst = state.isFirst,
                        isLast = state.isLast,
                        lineAlpha = lineAlpha,
                    )
                }
                .padding(vertical = 8.dp),
        ) {
            StopIndicator(alias = alias, alpha = iconAlpha)

            Text(
                text = stopName,
                color = colorResource(R.color.secondaryColor).copy(alpha = textAlpha),
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp)
                    .weight(1f),
            )

            if (state.currentDelay != -1) {
                DepartureTimeText(stop.stopTime, isLastStop = state.isLast, alpha = textAlpha)
            } else {
                Text(
                    text = stop.stopTime.formatWithoutDelay(!state.isLast),
                    color = colorResource(R.color.secondary_color_tone).copy(alpha = textAlpha),
                )
            }
        }
    }

    @Composable
    private fun DepartureTimeText(
        stopTime: StopTime,
        isLastStop: Boolean,
        alpha: Float = 1f,
    ) {
        val depSettings = LocalDeparturesSettings.current

        val arrival = stopTime.getArrival(false)
        val departure = stopTime.getDeparture(false)
        val delay = stopTime.delay

        if (stopTime.immediateDeparture() || isLastStop) {
            val time = arrival.addMinutes(delay).format()

            var delayStr = ""
            val color = Color(getDelayColor(delay)).copy(alpha = alpha)

            if (delay > 0) {
                when (depSettings.delayRender) {
                    DelayRenderType.PARENTHESES -> {
                        delayStr = " ($delay) "
                    }

                    DelayRenderType.BOX -> {
                        Surface(
                            color = color.copy(alpha = color.alpha * 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(" +$delay ", color = color, fontSize = 14.sp)
                        }
                    }

                    else -> {}
                }
            }

            Text("$delayStr$time", color = color)
            return
        }

        val loweredDelay = stopTime.loweredDelay
        val arrivalText = arrival.addMinutes(delay).format()
        val departureText = departure.addMinutes(loweredDelay).format()

        Row {
            Text(arrivalText, color = Color(getDelayColor(delay)).copy(alpha = alpha))
            Text(" - ", color = colorResource(R.color.secondary_color_tone).copy(alpha = alpha))
            Text(departureText, color = Color(getDelayColor(loweredDelay)).copy(alpha = alpha))
        }
    }

    @Composable
    private fun StopIndicator(
        alias: LineAlias,
        alpha: Float,
        modifier: Modifier = Modifier,
    ) {
        val iconSize = 20.dp

        Box(
            modifier
                .size(iconSize)
                .drawBehind {
                    val borderWidth = 2.dp.toPx()
                    val bgColor = Color(alias.backgroundColor)

                    drawCircle(color = bgColor.copy(alpha = alpha), blendMode = BlendMode.Src)

                    if (bgColor == Color.Black) {
                        drawCircle(
                            color = Color(alias.textColor).copy(alpha = alpha),
                            radius = size.minDimension / 2 - borderWidth / 2,
                            style = Stroke(width = borderWidth),
                            blendMode = BlendMode.Src,
                        )
                    }
                })
    }

    @Composable
    private fun rememberPulsingAlpha(): Float {
        val minAlpha = 0.2f
        val maxAlpha = 0.9f
        val durationMs = 750

        val transition = rememberInfiniteTransition()
        val alpha by transition.animateFloat(
            initialValue = minAlpha, targetValue = maxAlpha, animationSpec = infiniteRepeatable(
                animation = tween(durationMs),
                repeatMode = RepeatMode.Reverse,
            )
        )
        return alpha
    }


    private fun DrawScope.drawStopConnector(
        backgroundColor: Color,
        textColor: Color,
        isTransition: Boolean,
        isFirst: Boolean,
        isLast: Boolean,
        lineAlpha: Float,
    ) {
        val centerX = 10.dp.toPx()
        val fillWidth = 10.dp.toPx()
        val borderWidth = 2.dp.toPx()
        val endInset = 10.dp.toPx()

        val startY = if (isFirst) endInset else 0f
        val endY = size.height - if (isLast) endInset else 0f
        val borderOffset = fillWidth / 2 + borderWidth / 2

        fun drawVerticalLine(color: Color, strokeWidth: Float, dx: Float = 0f) {
            val start = Offset(centerX + dx, startY)
            val end = Offset(centerX + dx, endY)
            if (isTransition) {
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            color.copy(alpha = lineAlpha),
                        ),
                        startY = startY,
                        endY = endY,
                    ),
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                )
            } else {
                drawLine(
                    color = color.copy(alpha = lineAlpha),
                    start = start,
                    end = end,
                    strokeWidth = strokeWidth,
                )
            }
        }

        drawVerticalLine(backgroundColor, fillWidth)
        if (backgroundColor == Color.Black) {
            drawVerticalLine(textColor, borderWidth, dx = -borderOffset)
            drawVerticalLine(textColor, borderWidth, dx = borderOffset)
        }
    }

    private data class TripInfoData(
        val tripId: Int,
        val highlightedStopId: Int,
        val lineId: Int,
        val routeId: Int,
        val vehicleId: Int,
        val vehicleInfo: VehicleInfo,
        val headsign: String,
        val routeInfoText: String,
        val stops: Array<RouteStop>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TripInfoData

            if (tripId != other.tripId) return false
            if (highlightedStopId != other.highlightedStopId) return false
            if (lineId != other.lineId) return false
            if (routeId != other.routeId) return false
            if (vehicleId != other.vehicleId) return false
            if (vehicleInfo != other.vehicleInfo) return false
            if (headsign != other.headsign) return false
            if (routeInfoText != other.routeInfoText) return false
            if (!stops.contentEquals(other.stops)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tripId
            result = 31 * result + highlightedStopId
            result = 31 * result + lineId
            result = 31 * result + routeId
            result = 31 * result + vehicleId
            result = 31 * result + vehicleInfo.hashCode()
            result = 31 * result + headsign.hashCode()
            result = 31 * result + routeInfoText.hashCode()
            result = 31 * result + stops.contentHashCode()
            return result
        }
    }

}