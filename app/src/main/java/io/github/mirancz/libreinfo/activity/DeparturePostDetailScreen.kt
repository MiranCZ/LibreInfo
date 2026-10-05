package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.Post
import io.github.mirancz.libreinfo.parsing.types.Time
import io.github.mirancz.libreinfo.parsing.types.departure.Departure
import io.github.mirancz.libreinfo.util.OfflineDepartures
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.request.RequestHelper
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.parsing.types.dto.StopDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.ui.LocalSnackbarHostState
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.AsyncContent
import io.github.mirancz.libreinfo.ui.components.DepartureDetail
import io.github.mirancz.libreinfo.ui.components.DepartureEntryRowShimmer
import io.github.mirancz.libreinfo.ui.components.DeparturePostHeader
import io.github.mirancz.libreinfo.ui.components.rememberActivityShimmer
import io.github.mirancz.libreinfo.ui.showError
import io.github.mirancz.libreinfo.util.load.toAppException

@Composable
fun DeparturePostDetailScreen(state: NavState, stopId: Int, postId: Int) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current

    var post: Post? by remember { mutableStateOf(null) }
    var stopDelays by remember { mutableStateOf(StopDelaysResponse(emptyMap())) }

    val result = rememberLoad {
        val storage = AppContainer.storageProvider.getInstance()
        post = storage.postStorage.getPost(stopId, postId)

        var delays: RouteDelaysResponse? = null
        try {
            delays = RequestHelper.getRouteDelays(context)
        } catch (e: RequestException) {
            snackbar.showError(context, e.toAppException())
        }


        val originalId = storage.stopMapper.getOriginal(stopId)


        try {
            stopDelays = RequestHelper.getStopDelays(context, StopId(stopId, originalId))
        } catch (e: RequestException) {
            snackbar.showError(context, e.toAppException())
        }


        val departureList = OfflineDepartures.getOfflineForPost(
            storage,
            stopId,
            postId,
            -1,
            Time.ZERO,
            delays
        )

        val res = departureList.stream().filter { dep: Departure? -> dep!!.postID == postId }
            .findFirst().orElse(null)

        Pair(res, storage)
    }

    val _post = post
    val title = if (_post == null) "" else _post.name
    ScreenScaffold(title, onBack = state.onBack) {
        AsyncContent(result, loading = { DepartureDetailShimmer(title) }) { res ->
            DepartureDetail(
                res.first,
                res.second.apiStorage,
                stopDelays
            ) { vehicleInfo, stopId, tripId ->
                val vehicleId = if (vehicleInfo.hasId()) vehicleInfo.id() else null

                val route = NavRoute.TripDetail(vehicleId, stopId, tripId)
                state.onNavigate(route)
            }
        }
    }
}


@Composable
fun DepartureDetailShimmer(postName: String) {
    val shimmer = rememberActivityShimmer()
    val color = MaterialTheme.colorScheme.surfaceContainer
    Container(
        innerPadding = 0.dp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        LazyColumn(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
            stickyHeader {
                DeparturePostHeader(
                    postName, Modifier
                        .background(color)
                        .clickable(interactionSource = null, indication = null) {})

            }

            items(30) { _ ->
                DepartureEntryRowShimmer(shimmer)
            }
        }
    }
}
