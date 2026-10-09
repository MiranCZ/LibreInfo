package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.activity.settings.DepartureSource
import io.github.mirancz.libreinfo.engine.departure.OfflineDepartureRepository
import io.github.mirancz.libreinfo.engine.departure.ServerDepartureRepository
import io.github.mirancz.libreinfo.nav.NavRoute
import io.github.mirancz.libreinfo.nav.NavState
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureBoard
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureEntry
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureHeader
import io.github.mirancz.libreinfo.parsing.types.departure.PostDeparture
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.ui.LocalSnackbarHostState
import io.github.mirancz.libreinfo.ui.ScreenScaffold
import io.github.mirancz.libreinfo.ui.components.AsyncContent
import io.github.mirancz.libreinfo.ui.components.Container
import io.github.mirancz.libreinfo.ui.components.DepartureEntryShimmer
import io.github.mirancz.libreinfo.ui.components.FavouriteStopAction
import io.github.mirancz.libreinfo.ui.components.NothingHere
import io.github.mirancz.libreinfo.ui.components.PostDeparture
import io.github.mirancz.libreinfo.ui.components.StopViewModel
import io.github.mirancz.libreinfo.ui.components.rememberActivityShimmer
import io.github.mirancz.libreinfo.ui.showError
import io.github.mirancz.libreinfo.util.AppSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.load.rememberLoad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun DeparturesScreen(state: NavState, stopId: Int) {
    val vm: StopViewModel = viewModel()
    val context = LocalContext.current

    val departuresSettings = LocalDeparturesSettings.current
    val snackbar = LocalSnackbarHostState.current

    val repo = when (AppSettings.Departures.source) {
        DepartureSource.LOCAL -> {
            OfflineDepartureRepository(AppContainer.storageProvider)
        }
        DepartureSource.SERVER -> {
            ServerDepartureRepository(AppContainer.storageProvider)
        }
    }

    var header: DepartureHeader? by remember { mutableStateOf(repo.headerOrNull(context, stopId)) }

    if (header == null) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                header = repo.header(context, stopId)
            }
        }
    }

    val departuresResult = rememberLoad {
        repo.board(context, stopId, departuresSettings.maxEntries, isRefresh)
    }

    val title = header?.stopName ?: ""

    LaunchedEffect(header?.favourite) {
        if (header?.favourite == true) {
            vm.setLiked(true)
        } else {
            vm.setLiked(false)
        }
    }

    ScreenScaffold(title, onBack = state.onBack, actions = {
        FavouriteStopAction(StopId(stopId))
    }) {
        PullToRefreshBox(departuresResult.isRefreshing, departuresResult.refresh) {
            AsyncContent(departuresResult, loading = { DeparturesShimmer(header?.postNames) }) { deps ->

                deps.error?.let { err ->
                    LaunchedEffect(Unit) {
                        snackbar.showError(context, err)
                    }
                }

                if (deps.postDepartures.isEmpty()) {
                    NothingHere()
                } else {
                    DepartureBoard(deps, onPostClick = {
                        state.onNavigate(
                            NavRoute.Departures.PostDetail(
                                deps.stop.id.id,
                                it.postId
                            )
                        )
                    }) {
                        val vehicleInfo = it.vehicleInfo
                        val vehicleId = if (vehicleInfo.hasId()) vehicleInfo.id() else null

                        // FIXME is it trip or route id???
                        it.tripId?.let { tripId ->
                            val route = NavRoute.TripDetail(vehicleId, stopId, routeId = tripId)
                            state.onNavigate(route)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DepartureBoard(
    board: DepartureBoard,
    onPostClick: (PostDeparture) -> Unit,
    onEntryClick: (DepartureEntry) -> Unit
) {
    LazyColumn {
        val message = board.message
        if (!message.isNullOrBlank()) {
            item {
                Container(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    innerPadding = 0.dp
                ) {
                    Text(
                        message,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        }

        items(board.postDepartures) { entry ->
            PostDeparture(entry, onHeaderClick = {
                onPostClick(entry)
            }) {
                onEntryClick(it)
            }
        }
    }
}


@Composable
fun DeparturesShimmer(postNames: List<String>?) {
    val shimmer = rememberActivityShimmer()

    val entries: List<String?> = postNames ?: listOf(null, null)
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

