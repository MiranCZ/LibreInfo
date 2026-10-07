package io.github.mirancz.libreinfo.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.settings.DelayRenderType
import io.github.mirancz.libreinfo.parsing.storage.ApiStorage
import io.github.mirancz.libreinfo.parsing.types.Time
import io.github.mirancz.libreinfo.parsing.types.departure.Departure
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureEntry
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureTime
import io.github.mirancz.libreinfo.parsing.types.departure.PostDeparture
import io.github.mirancz.libreinfo.parsing.types.departure.VehicleInfo
import io.github.mirancz.libreinfo.parsing.types.dto.StopDelaysResponse
import io.github.mirancz.libreinfo.ui.theme.extendedColors
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings

@Composable
fun DeparturePostHeader(
    name: String, modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )

        Divider(
            Modifier
                .padding(horizontal = 10.dp)
                .padding(top = 4.dp)
        )
    }
}

@Composable
fun DepartureEntryRowShimmer(shimmer: Shimmer) {
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(3f), verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerLineIcon(shimmer)
            Spacer(Modifier.width(4.dp))
            ShimmerText(shimmer, widthFraction = 0.55f, variance = 0.2f)
        }
        Row(
            Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            ShimmerText(shimmer, widthFraction = 0.85f, variance = 0.1f)
        }
    }
}

@Composable
fun DepartureEntryShimmer(shimmer: Shimmer, postName: String?, repeat: Int = 5) {
    Container(
        innerPadding = 0.dp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
            Column(Modifier.padding(bottom = 4.dp)) {
                Crossfade(targetState = postName) { name ->
                    if (name != null) {
                        Text(
                            name,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        )
                    } else {
                        Box(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        ) {
                            ShimmerText(
                                shimmer, height = 18.dp, widthFraction = 0.4f, variance = 0.15f
                            )
                        }
                    }
                }
                Divider(
                    Modifier
                        .padding(horizontal = 10.dp)
                        .padding(top = 4.dp)
                )
            }

            repeat(repeat) {
                DepartureEntryRowShimmer(shimmer)
            }
        }
    }
}

@Composable
fun DepartureEntry(
    departure: DepartureEntry,
    modifier: Modifier = Modifier,
    showDelay: Boolean = true,
    onClick: (() -> Unit)?
) {
    val vehicleInfo = departure.vehicleInfo
    val depSettings = LocalDeparturesSettings.current

    Box(
        modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(null, ripple(), onClick = onClick)
                } else Modifier
            )
            .padding(horizontal = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Row(Modifier.weight(3f), verticalAlignment = Alignment.CenterVertically) {
                LineIcon(line = departure.line)
                Text(
                    departure.finalStop,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 4.dp)
                        .weight(1f)
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }

            if (departure.lowFloor && depSettings.showLowFloor) {
                Icon(
                    painter = painterResource(R.drawable.wheelchair_regular),
                    "lowfloor",
                    Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically),
                    tint = MaterialTheme.extendedColors.onSurfaceMedium
                )
            }

            Row(
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                DepartureTimeText(vehicleInfo, showDelay, departure)
            }
        }
    }
}

@Composable
private fun RowScope.DepartureTimeText(
    vehicleInfo: VehicleInfo,
    showDelay: Boolean,
    departure: DepartureEntry,
) {
    val depSettings = LocalDeparturesSettings.current
    val time = departure.time

    if (vehicleInfo.hasDelay() && showDelay) {
        val delay: Int = vehicleInfo.delay()
        val color: Int = vehicleInfo.delayColor

        val arrivalText: String = when(time) {
            is DepartureTime.Scheduled -> {
                val timeMark = time.mark

                timeMark.delay = delay
                timeMark.getFormattedDepartureString(30, true)
            }
            is DepartureTime.Verbatim -> {
                time.text
            }
        }

        Spacer(Modifier.weight(1f))

        if (delay > 0) {
            when (depSettings.delayRender) {
                DelayRenderType.PARENTHESES -> {
                    Text(" ($delay) ", color = Color(color), fontSize = 14.sp)
                }

                DelayRenderType.BOX -> {
                    Surface(
                        color = Color(color).copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(" +$delay ", color = Color(color), fontSize = 14.sp)
                    }
                }

                else -> {}
            }
        }

        if (arrivalText == "**") {
            BlinkingText(arrivalText, color = Color(color), fontSize = 14.sp)
        } else {
            Text(arrivalText, color = Color(color), fontSize = 14.sp)
        }
    } else {
        val arrivalText: String = when (time) {
            is DepartureTime.Scheduled -> {
                time.mark.getFormattedDepartureString(30, false)
            }
            is DepartureTime.Verbatim -> {
                time.text
            }
        }

        Spacer(Modifier.weight(1f))
        if (arrivalText == "**") {
            BlinkingText(arrivalText, fontSize = 14.sp)
        } else {
            Text(arrivalText, fontSize = 14.sp)
        }
    }
}

@Composable
fun DepartureDetail(
    departure: PostDeparture,
    apiStorage: ApiStorage,
    stopDelays: StopDelaysResponse,
    onEntryClick: (DepartureEntry) -> Unit
) {
    val color = MaterialTheme.colorScheme.surfaceContainer
    val stopDelays = stopDelays.stopDelays

    fun alreadyLeft(entry: DepartureEntry): Boolean {
        if (entry.time is DepartureTime.Scheduled) {
            val mark = entry.time.mark

            return mark.delayedDeparture.isBefore(Time.now()) && !mark.leaving
        }

        return false
    }

    Container(
        innerPadding = 0.dp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val first = departure.entries.indexOfFirst { entry -> !alreadyLeft(entry) }
        val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = first)

        LazyColumn(
            Modifier.padding(vertical = 8.dp, horizontal = 6.dp), state = lazyListState
        ) {
            stickyHeader {
                DeparturePostHeader(
                    departure.name,
                    Modifier
                        .background(color)
                        .clickable(interactionSource = null, indication = null) {})
            }
            items(departure.entries) { entry ->
                val alreadyLeft = alreadyLeft(entry)

                var modifier: Modifier = Modifier

                if (alreadyLeft) {
                    modifier = modifier.alpha(0.35f)
                }

                var showDelay = !alreadyLeft
                if (entry.tripId != null) {
                    val lineRoute = apiStorage.getLineIdAndRoute(entry.tripId)

                    val lineId = lineRoute.left
                    val routeId = lineRoute.right

                    if (alreadyLeft) {
                        var delay = -1

                        val delays = stopDelays[lineId]
                        if (delays != null) {
                            val delayEntry = delays[routeId]

                            if (delayEntry != null) {
                                delay = delayEntry.delay
                            }
                        }
                        entry.vehicleInfo.setDelay(delay)

                        showDelay = delay != -1
                    }
                }

                val onClick = if (entry.tripId != null) { { onEntryClick(entry) } } else null
                DepartureEntry(entry, modifier, showDelay, onClick)
            }
        }
    }
}

@Composable
fun PostDeparture(
    departure: PostDeparture,
    onHeaderClick: (() -> Unit)? = null,
    onEntryClick: ((DepartureEntry) -> Unit)?
) {
    val content: @Composable BoxScope.() -> Unit = {
        Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
            DeparturePostHeader(departure.name, Modifier.padding(bottom = 4.dp))
            val depSettings = LocalDeparturesSettings.current
            for (dep in departure.entries.take(depSettings.maxEntries)) {
                val onClick = if (onEntryClick == null || dep.tripId == null) null else {
                    { onEntryClick(dep) }
                }

                DepartureEntry(dep, onClick = onClick)
            }
        }
    }
    val mod = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

    Container( if(departure.detailAvailable) onHeaderClick else null, innerPadding = 0.dp, modifier = mod, content = content)
}


/**
 * Pulses [text] so a vehicle that is leaving right now draws the eye. Kept as its own composable
 * so the infinite animation only ever exists for the rows that are actually leaving.
 */
@Composable
private fun BlinkingText(text: String, color: Color = Color.Unspecified, fontSize: TextUnit) {
    val transition = rememberInfiniteTransition(label = "leaving")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "leavingAlpha"
    )

    Text(text, color = color, fontSize = fontSize, modifier = Modifier.alpha(alpha))
}