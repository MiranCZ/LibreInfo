package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.activity.base.snackbar.CustomSnackBarVisuals
import io.github.mirancz.libreinfo.activity.base.snackbar.SnackBarType
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.departure.Departures
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.parsing.types.stop.isFavourite
import io.github.mirancz.libreinfo.ui.LocalSnackbarHostState
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.AsyncContent
import io.github.mirancz.libreinfo.ui.components.Departure
import io.github.mirancz.libreinfo.ui.components.DepartureEntryShimmer
import io.github.mirancz.libreinfo.ui.components.FavouriteStopAction
import io.github.mirancz.libreinfo.ui.components.NothingHere
import io.github.mirancz.libreinfo.ui.components.StopViewModel
import io.github.mirancz.libreinfo.ui.components.rememberActivityShimmer
import io.github.mirancz.libreinfo.ui.showError
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.OfflineDepartures
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.load.toAppException
import io.github.mirancz.libreinfo.util.request.RequestHelper


@Composable
fun DeparturesScreen(state: NavState, stopId: Int) {
    val vm: StopViewModel = viewModel()
    val context = LocalContext.current

    val provider = AppContainer.storageProvider
    var storage: IdStorage? by remember { mutableStateOf(provider.getInstanceOrNull()) }
    var stop: Stop by remember { mutableStateOf(Stop.NONE) }

    LaunchedEffect(stop) {
        val local = stop
        if (local.isFavourite()) {
            vm.setLiked(true)
        }
    }


    val departuresSettings = LocalDeparturesSettings.current
    val snackbar = LocalSnackbarHostState.current

    var delays: RouteDelaysResponse? by remember { mutableStateOf(null) }
//    var delays = DelaysDataHolder.getDelays()

    val departuresResult = rememberLoad {
        try {
            delays = RequestHelper.getRouteDelays(context, force = isRefresh)
        } catch (e: RequestException) {

            snackbar.showError(context, e.toAppException())
        }

        val _storage = provider.getInstance()
        storage = _storage

        stop = if (stopId != -1) {
            _storage.stopStorage.getStop(StopId.internal(stopId))
        } else {
            Stop.NONE
        }

        Departures("Work in progress...", OfflineDepartures.getOffline(
            storage,
            stopId,
            departuresSettings.maxEntries,
            delays
        ))
    }

    val title = if (stop == Stop.NONE) "" else stop.name
    ScreenScaffold(title, onBack = state.onBack, actions = {
        FavouriteStopAction(StopId(stopId, -1))
    }) {
        PullToRefreshBox(departuresResult.isRefreshing, departuresResult.refresh) {
        AsyncContent(departuresResult, loading = { DeparturesShimmer(storage, stop) }) { deps ->
            if (deps.departures.isEmpty()) {
                NothingHere()
            } else {
                Departures(state, deps, stop, storage!!)
                }
            }
        }
    }
}


@Composable
fun Departures(state: NavState, departures: Departures, stop: Stop, storage: IdStorage) {
    LazyColumn {
        items(departures.departures) { entry ->
            val post = storage.postStorage.getPost(stop.id.internal, entry.postID);

            Departure(entry, post, onHeaderClick = {
                state.onNavigate(NavRoute.Departures.PostDetail(stop.id.internal, entry.postID))
            }) { vehicleInfo, stopId, tripId ->
                val vehicleId = if (vehicleInfo.hasId()) vehicleInfo.id() else null

                val route = NavRoute.TripDetail(vehicleId, stopId, tripId)
                state.onNavigate(route)
            }
        }
    }
}

@Composable
fun DeparturesShimmer(storage: IdStorage?, stop: Stop) {
    val shimmer = rememberActivityShimmer()

    val entries: List<String?> = storage?.postStorage?.getPosts(stop)?.map { it.name } ?: listOf(null, null)
    val settings = LocalDeparturesSettings.current

    LazyColumn {
        items(entries) { postName ->
            DepartureEntryShimmer(
                shimmer,
                postName = postName,
                repeat = settings.maxEntries
            )
        }
    }
}
