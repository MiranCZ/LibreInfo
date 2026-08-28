package io.github.mirancz.libreinfo.activity

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.activity.component.Container
import io.github.mirancz.libreinfo.activity.component.FavouriteStopAction
import io.github.mirancz.libreinfo.activity.component.StopViewModel
import io.github.mirancz.libreinfo.activity.data.DelaysDataHolder
import io.github.mirancz.libreinfo.activity.settings.DelayRenderType
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.dto.ServerDepartureDTO
import io.github.mirancz.libreinfo.parsing.types.dto.ServerPostDTO
import io.github.mirancz.libreinfo.parsing.types.dto.mapLine
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.response.ServerDeparturesResponse
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.util.DelayUtil
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.Text
import io.github.mirancz.libreinfo.util.load.LoadState
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.request.RequestHelper

/**
 * Departure boards taken verbatim from the app server. Unlike [DeparturesActivity], the entries are
 * not computed from the bundled timetables, so there is no whole day to open and no delay to render
 * on top of them - [ServerDepartureDTO.time] is already final.
 */
class ServerDeparturesActivity : KBaseActivity("") {

    private var departuresSettings by mutableStateOf(DeparturesSettings())

    override fun onResume() {
        super.onResume()
        departuresSettings = DeparturesSettings.fromPrefs()
    }

    @Composable
    override fun CreateElements() {
        val stop = intent.getParcelableExtra<Stop>("stop")!!
        val context = LocalContext.current
        val vm: StopViewModel = viewModel()

        val provider = AppContainer.storageProvider
        var storage: IdStorage? by remember { mutableStateOf(provider.getInstanceOrNull()) }

        LaunchedEffect(Unit) {
            if (stop.isFavourite) {
                vm.setLiked(true)
            }
        }

        // the delays only tint an already final time, so the prefetch from SearchActivity is good
        // enough to open with - a refresh is what pulls a fresh feed
        var delays by remember { mutableStateOf(DelaysDataHolder.getDelays()) }
        var refreshTick by remember { mutableIntStateOf(0) }

        val result = rememberLoad(refreshTick) {
            storage = provider.getInstance()

            if (refreshTick > 0) {
                try {
                    delays = RequestHelper.getRouteDelays(context)
                } catch (e: RequestException) {
                    showErrorSnackBar(e)
                }
            }

            RequestHelper.getDepartures(context, stop.id)
        }

        val refreshing by vm.refreshing
        LaunchedEffect(result.state) {
            if (result.state !is LoadState.Loading) vm.setRefreshing(false)
        }

        CompositionLocalProvider(LocalDeparturesSettings provides departuresSettings) {
            PullToRefreshBox(refreshing, {
                vm.setRefreshing(true)
                refreshTick++
            }) {
                AsyncContent(result, loading = { DeparturesShimmer(storage) }) { departures ->
                    Departures(departures, storage!!, delays)
                }
            }
        }
    }

    override fun setBaseContent(
        actions: @Composable (RowScope.() -> Unit),
        content: @Composable (() -> Unit)
    ) {
        val stop = intent.getParcelableExtra<Stop>("stop")!!

        name = Text.literal(stop.name)

        super.setBaseContent({
            actions()
            FavouriteStopAction(stop)
        }, content)
    }

    @Composable
    private fun Departures(
        departures: ServerDeparturesResponse,
        storage: IdStorage,
        delays: RouteDelaysResponse?
    ) {
        val stop = intent.getParcelableExtra<Stop>("stop")!!

        val error = departures.metadata?.error
        LaunchedEffect(error) {
            if (!error.isNullOrBlank()) showSnackBar(error, SnackBarType.ERROR)
        }

        if (departures.posts.isEmpty()) {
            NothingHere()
            return
        }

        LazyColumn {
            val message = departures.message
            if (!message.isNullOrBlank()) {
                item {
                    Container(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = colorResource(R.color.ui_warning),
                        innerPadding = 0.dp
                    ) {
                        Text(
                            message,
                            color = colorResource(R.color.secondaryColor),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            items(departures.posts) { post ->
                ServerPost(post, storage, stop, delays)
            }
        }
    }

    @Composable
    private fun ServerPost(
        post: ServerPostDTO,
        storage: IdStorage,
        stop: Stop,
        delays: RouteDelaysResponse?
    ) {
        val depSettings = LocalDeparturesSettings.current

        // no click target: the server only returns the current board, there is no whole day to open
        Container(
            innerPadding = 0.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                DeparturePostHeader(post.name, Modifier.padding(bottom = 4.dp))

                for (departure in post.departures.take(depSettings.maxEntries)) {
                    ServerDepartureRow(departure, storage, stop, delays)
                }
            }
        }
    }

    @Composable
    private fun ServerDepartureRow(
        departure: ServerDepartureDTO,
        storage: IdStorage,
        stop: Stop,
        delays: RouteDelaysResponse?
    ) {
        val depSettings = LocalDeparturesSettings.current

        val tripId = remember(departure.lineId, departure.routeId) {
            storage.apiStorage.getTripId(departure.lineId, departure.routeId)
        }

        var modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))

        if (tripId >= 0) {
            modifier = modifier.clickable(null, ripple()) {
                startActivity(TripDetailActivity::class) { intent: Intent ->
                    intent.putExtra("stopId", stop.id.internal)
                    intent.putExtra("tripId", tripId)
                }
            }
        }

        Row(modifier.padding(horizontal = 8.dp).fillMaxWidth()) {
            Row(Modifier.weight(3f)) {
                LineIcon(line = mapLine(storage, departure.lineId))
                Text(
                    departure.finalStop,
                    fontSize = 14.sp,
                    color = colorResource(R.color.secondaryColor),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 4.dp)
                )
            }

            if (departure.isLowFloor && depSettings.showLowFloor) {
                Icon(
                    painter = painterResource(R.drawable.wheelchair_regular),
                    "lowfloor",
                    Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically),
                    tint = colorResource(R.color.secondary_color_light_tone)
                )
            }

            Row(
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Spacer(Modifier.weight(1f))
                DepartureTime(departure.time, delays?.delayFor(departure))
            }
        }
    }

    /**
     * Renders the server's already final time string. A delay only picks the colour and fills the
     * marker next to it - it is never added to the time.
     */
    @Composable
    private fun DepartureTime(time: String, delay: Int?) {
        if (delay == null) {
            if (time == LEAVING_MARK) BlinkingText(time, fontSize = 14.sp) else Text(time, fontSize = 14.sp)
            return
        }

        val depSettings = LocalDeparturesSettings.current
        val color = Color(DelayUtil.getDelayColor(delay))

        var prefix = ""
        if (delay > 0) {
            when (depSettings.delayRender) {
                DelayRenderType.PARENTHESES -> prefix = " ($delay) "
                DelayRenderType.BOX -> Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(" +$delay ", color = color, fontSize = 14.sp)
                }
                else -> {}
            }
        }

        if (time == LEAVING_MARK) {
            BlinkingText(prefix + time, color = color, fontSize = 14.sp)
        } else {
            Text(prefix + time, color = color, fontSize = 14.sp)
        }
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

    private companion object {
        /** What the boards print instead of a time when the vehicle is departing right now. */
        const val LEAVING_MARK = "**"

        /** The server departure carries the very line/route pair the delay feed is keyed by. */
        fun RouteDelaysResponse.delayFor(departure: ServerDepartureDTO): Int? =
            routeDelays[departure.lineId]?.get(departure.routeId)?.delay
    }

    @Composable
    private fun DeparturesShimmer(storage: IdStorage?) {
        val stop = intent.getParcelableExtra<Stop>("stop")!!
        val shimmer = rememberActivityShimmer()

        val entries: List<String?> =
            storage?.postStorage?.getPosts(stop)?.map { it.name } ?: listOf(null, null)

        LazyColumn {
            items(entries) { postName ->
                DepartureEntryShimmer(shimmer, postName = postName, repeat = departuresSettings.maxEntries)
            }
        }
    }
}
