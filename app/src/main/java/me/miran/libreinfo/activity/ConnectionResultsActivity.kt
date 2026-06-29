package me.miran.libreinfo.activity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.manager.AppContainer
import me.miran.libreinfo.parsing.storage.manager.IdStorage
import me.miran.libreinfo.parsing.types.DateTime
import me.miran.libreinfo.parsing.types.connection.Connection
import me.miran.libreinfo.parsing.types.stop.Stop
import me.miran.libreinfo.util.load.rememberLoad
import me.miran.libreinfo.util.request.RequestHelper

class ConnectionResultsActivity : KBaseActivity(R.string.connection_results) {

    @Composable
    override fun CreateElements() {
        val fromStop = intent.getParcelableExtra<Stop>("fromStop")!!
        val toStop = intent.getParcelableExtra<Stop>("toStop")!!
        val departureTime = intent.getStringExtra("departureTime")!!

        val context = LocalContext.current

        val result = rememberLoad(fromStop, toStop, departureTime) {
            val storage = AppContainer.storageProvider.getInstance()
            val obj = RequestHelper.findConnections(context, fromStop, toStop, departureTime)
            val now = DateTime.now()
            obj.getAsJsonArray("connections").map { element ->
                buildConnectionUi(Connection.parse(element.asJsonObject, storage.stopStorage), storage, now)
            }
        }

        AsyncContent(result, loading = { ConnectionsShimmer() }) { connections ->
            if (connections.isEmpty()) {
                NothingHere()
            } else {
                LazyColumn {
                    items(connections) { connection ->
                        ConnectionCard(connection)
                    }
                }
            }
        }
    }

    @Composable
    private fun ConnectionCard(connection: ConnectionUi) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column(Modifier.fillMaxWidth()) {
                ConnectionHeader(connection)
                Spacer(Modifier.height(10.dp))
                for (leg in connection.legs) {
                    when (leg) {
                        is WalkLegUi -> WalkLeg(leg)
                        is VehicleLegUi -> VehicleLeg(leg)
                    }
                }
            }
        }
    }

    @Composable
    private fun ConnectionHeader(connection: ConnectionUi) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {

            val countdown = if (connection.countdownMin <= 0) {
                stringResource(R.string.connection_now)
            } else {
                stringResource(R.string.connection_in, formatMinutes(connection.countdownMin))
            }
            Text(
                countdown,
                color = colorResource(R.color.light_blue),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(connection.depTime, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        " - ",
                        fontSize = 12.sp,
                        color = colorResource(R.color.secondary_color_tone)
                    )
                    Text(connection.arrTime, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    formatMinutes(connection.durationMin),
                    color = colorResource(R.color.secondary_color_tone),
                    fontSize = 13.sp
                )
            }
        }
    }

    @Composable
    private fun VehicleLeg(leg: VehicleLegUi) {
        val lineColor = Color(leg.alias.backgroundColor)
        val bg = colorResource(R.color.widget_background)

        val openTripDetail = {
            startActivity(TripDetailActivity::class) { intent ->
                intent.putExtra("tripId", leg.tripId)
                intent.putExtra("stopId", leg.boardStopInternalId)
            }
        }

        Column(Modifier.clickable(onClick = openTripDetail)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIcon(line = leg.alias, padding = 0.dp)

                Text(
                    leg.headsign,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }

            TimelineRow(lineColor, bg, Dot.FILLED, connectTop = false, connectBottom = true) {
                StopText(leg.boardTime, leg.boardStop)
            }
            TimelineRow(lineColor, bg, Dot.NONE, connectTop = true, connectBottom = true) {
                Text(
                    stringResource(R.string.connection_stops, leg.stopCount),
                    color = colorResource(R.color.secondary_color_tone),
                    fontSize = 13.sp
                )
            }
            TimelineRow(lineColor, bg, Dot.HOLLOW, connectTop = true, connectBottom = false) {
                StopText(leg.alightTime, leg.alightStop)
            }
        }
    }

    @Composable
    private fun WalkLeg(leg: WalkLegUi) {
        val label = when (leg.kind) {
            WalkKind.TO_STOP -> stringResource(R.string.connection_walk_to_stop)
            WalkKind.TRANSFER -> stringResource(R.string.connection_walk_transfer)
            WalkKind.TO_DESTINATION -> stringResource(R.string.connection_walk_to_destination)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            WalkGlyph()

            val text = if (leg.distance >= 0) {
                "$label ${formatMinutes(leg.durationMin)} (${leg.distance} m)"
            } else {
                "$label ${formatMinutes(leg.durationMin)}"
            }
            Text(
                text,
                color = colorResource(R.color.secondary_color_tone),
                fontSize = 13.sp
            )
        }
    }

    @Composable
    private fun RowScope.StopText(time: String, stop: String) {
        Text(
            time,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            stop,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }

    private enum class Dot { FILLED, HOLLOW, NONE }

    @Composable
    private fun TimelineRow(
        connectorColor: Color,
        bg: Color,
        dot: Dot,
        connectTop: Boolean,
        connectBottom: Boolean,
        content: @Composable RowScope.() -> Unit,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    val cx = 12.dp.toPx()
                    val cy = size.height / 2
                    val stroke = 3.dp.toPx()
                    if (connectTop) {
                        drawLine(connectorColor, Offset(cx, 0f), Offset(cx, cy), stroke)
                    }
                    if (connectBottom) {
                        drawLine(connectorColor, Offset(cx, cy), Offset(cx, size.height), stroke)
                    }
                    val radius = 6.dp.toPx()
                    when (dot) {
                        Dot.FILLED -> drawCircle(connectorColor, radius, Offset(cx, cy))
                        Dot.HOLLOW -> {
                            drawCircle(bg, radius, Offset(cx, cy))
                            drawCircle(connectorColor, radius, Offset(cx, cy), style = Stroke(2.5.dp.toPx()))
                        }

                        Dot.NONE -> {}
                    }
                }
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(24.dp))
            content()
        }
    }

    @Composable
    private fun WalkGlyph() {
        Icon(
            painter = painterResource(R.drawable.person_walking_solid),
            contentDescription = null,
            tint = colorResource(R.color.secondary_color_tone),
            modifier = Modifier.size(22.dp)
        )
    }

    @Composable
    private fun ConnectionsShimmer() {
        val shimmer = rememberActivityShimmer()
        Column {
            repeat(5) {
                ConnectionCardShimmer(shimmer)
            }
        }
    }

    @Composable
    private fun ConnectionCardShimmer(shimmer: Shimmer) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ShimmerText(shimmer, widthFraction = 0.4f, variance = 0.1f, height = 22.dp)
                    Spacer(Modifier.weight(1f))
                    ShimmerText(shimmer, widthFraction = 0.2f, variance = 0.1f, height = 14.dp)
                }
                Spacer(Modifier.height(12.dp))
                repeat(2) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerLineIcon(shimmer)
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            ShimmerText(shimmer, widthFraction = 0.55f, variance = 0.15f, height = 15.dp)
                            Spacer(Modifier.height(3.dp))
                            ShimmerText(shimmer, widthFraction = 0.7f, variance = 0.15f, height = 12.dp)
                        }
                        ShimmerBox(Modifier.width(40.dp).height(14.dp), shimmer)
                    }
                }
            }
        }
    }
}
