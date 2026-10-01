package io.github.mirancz.libreinfo.activity

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mirancz.libreinfo.activity.base.KBaseActivity
import io.github.mirancz.libreinfo.ui.components.FavouriteStopAction
import io.github.mirancz.libreinfo.ui.components.StopViewModel
import io.github.mirancz.libreinfo.activity.data.DelaysDataHolder
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.manager.AppContainer
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.departure.Departures
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.util.DeparturesSettings
import io.github.mirancz.libreinfo.util.LocalDeparturesSettings
import io.github.mirancz.libreinfo.util.OfflineDepartures
import io.github.mirancz.libreinfo.util.load.rememberLoad
import io.github.mirancz.libreinfo.util.request.RequestHelper
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.parsing.types.stop.isFavourite
import io.github.mirancz.libreinfo.ui.components.AsyncContent
import io.github.mirancz.libreinfo.ui.components.DepartureEntryShimmer
import io.github.mirancz.libreinfo.ui.components.NothingHere
import io.github.mirancz.libreinfo.ui.components.rememberActivityShimmer


class DeparturesActivity : KBaseActivity("") {

    private var departuresSettings by mutableStateOf(DeparturesSettings())

    override fun onResume() {
        super.onResume()
        departuresSettings = DeparturesSettings.fromPrefs()
    }

    @Composable
    override fun CreateElements() {
//        val stop = intent.getParcelableExtra<Stop>("stop")!!
        val vm: StopViewModel = viewModel()

        val provider = AppContainer.storageProvider
        var storage: IdStorage? by remember { mutableStateOf(provider.getInstanceOrNull()) }
        var stop: Stop by remember { mutableStateOf(Stop.NONE) }

        LaunchedEffect(stop) {
            val local = stop
            if (local.isFavourite()) {
                vm.setLiked(true)
            }
        }

        val delays = DelaysDataHolder.getDelays()
        val departuresResult = rememberLoad {
            val _storage = provider.getInstance()
            storage = _storage

            val stopId = intent.getIntExtra("stop", -1)

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

        AsyncContent(departuresResult, loading = { DeparturesShimmer(storage, stop) }) { deps ->
            if (!deps.departures.isEmpty()) {
                this.Departures(deps, stop, storage!!)
            } else {
                NothingHere()
            }
        }
    }

    fun createDepartures(stop: Stop, refreshDelays: Boolean = false, onFinish: () -> Unit = {}) {
        var delays = DelaysDataHolder.getDelays()
        Thread {
            if (refreshDelays) {
                try {
                    delays = RequestHelper.getRouteDelays(this)
                } catch (e: RequestException) {
                    showErrorSnackBar(e)
                    delays = RouteDelaysResponse(emptyMap())
                }
            }

            val storage = AppContainer.storageProvider.getBlocking(IdStorage::class.java)
            val departures = Departures(
                "Work in progress...",
                OfflineDepartures.getOffline(
                    storage,
                    stop.id.internal,
                    departuresSettings.maxEntries,
                    delays
                )
            )

            runOnUiThread {
                setBaseContent {
                    if (!departures.departures.isEmpty()) {
                        this.Departures(departures, stop, storage)
                    } else {
                        NothingHere()
                    }
                }
                onFinish()
            }
        }.start()
    }

    override fun setBaseContent(
        actions: @Composable (RowScope.() -> Unit),
        content: @Composable (() -> Unit)
    ) {
        // FIXME not ideal at all; should be resolved later in refactoring
//        val stop = intent.getParcelableExtra<Stop>("stop")!!
//        name = Text.literal(stop.name)

        val stopId = intent.getIntExtra("stop", -1)

        super.setBaseContent({
            actions()

            // FIXME not ideal at all; should be resolved later in refactoring
            FavouriteStopAction(StopId(stopId, -1))
        }, content)
    }

    @Composable
    fun Departures(departures: Departures, stop: Stop, storage: IdStorage) {
        val vm: StopViewModel = viewModel()
        val refreshing by vm.refreshing

        CompositionLocalProvider(LocalDeparturesSettings provides departuresSettings) {
            PullToRefreshBox(refreshing, {
                vm.setRefreshing(true)

                createDepartures(stop, true) {
                    vm.setRefreshing(false)
                }
            }) {
                LazyColumn {
                    items(departures.departures) { entry ->
                        val post = storage.postStorage.getPost(stop.id.internal, entry.postID);

                        Departure(entry, post)
                    }
                }
            }
        }
    }

    @Composable
    fun DeparturesShimmer(storage: IdStorage?, stop: Stop) {
        val shimmer = rememberActivityShimmer()

        val entries: List<String?> = storage?.postStorage?.getPosts(stop)?.map { it.name } ?: listOf(null, null)

        LazyColumn {
            items(entries) { postName ->
                DepartureEntryShimmer(
                    shimmer,
                    postName = postName,
                    repeat = departuresSettings.maxEntries
                )
            }
        }
    }





}