package me.miran.libreinfo.activity

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.Shimmer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.activity.data.DelaysDataHolder
import me.miran.libreinfo.exception.RequestException
import me.miran.libreinfo.parsing.storage.IdStorage
import me.miran.libreinfo.parsing.types.departure.Departures
import me.miran.libreinfo.parsing.types.stop.Stop
import me.miran.libreinfo.util.DeparturesSettings
import me.miran.libreinfo.util.LocalDeparturesSettings
import me.miran.libreinfo.util.OfflineDepartures
import me.miran.libreinfo.util.Text
import me.miran.libreinfo.util.request.RequestHelper


class DeparturesActivity : KBaseActivity("") {

    private var departuresSettings by mutableStateOf(DeparturesSettings())

    override fun onResume() {
        super.onResume()
        departuresSettings = DeparturesSettings.fromPrefs()
    }

    class StopViewModel : ViewModel() {
        private val _liked = mutableStateOf(false)
        private val _isRefreshing = mutableStateOf(false)
        val liked = _liked
        val refreshing = _isRefreshing

        fun toggleLiked() {
            _liked.value = !_liked.value
        }

        fun setLiked(value: Boolean) {
            _liked.value = value
        }

        fun setRefreshing(value: Boolean) {
            _isRefreshing.value = value
        }

    }

    @Composable
    override fun CreateElements() {
        val stop = intent.getParcelableExtra<Stop>("stop")!!
        val vm: StopViewModel = viewModel()

        var storage: IdStorage? by remember { mutableStateOf(IdStorage.getInstanceOrNull()) }
        var departuresResult: Departures? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            if (stop.isFavourite) {
                vm.setLiked(true)
            }

            val s = withContext(Dispatchers.IO) {
                IdStorage.getInstance()
            }
            storage = s

            val delays = DelaysDataHolder.getDelays()
            val deps = withContext(Dispatchers.IO) {
                Departures("Work in progress...", OfflineDepartures.getOffline(
                    s,
                    stop.id.internal,
                    departuresSettings.maxEntries,
                    delays
                ))
            }
            departuresResult = deps
        }

        Crossfade(targetState = departuresResult) { deps ->
            if (deps != null) {
                if (!deps.departures.isEmpty()) {
                    this.Departures(deps, storage!!)
                } else {
                    NothingHere()
                }
            } else {
                DeparturesShimmer(storage)
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
                    delays = JsonObject()
                }
            }

            val storage = IdStorage.getInstance()
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
                        this.Departures(departures, storage)
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
        val stop = intent.getParcelableExtra<Stop>("stop")!!

        name = Text.literal(stop.name)

        super.setBaseContent({
            actions()
            val vm: StopViewModel = viewModel()
            val liked by vm.liked

            IconButton(onClick = {
                vm.toggleLiked()
                stop.setFavourite(liked)
                // FIXME flushing everytime is not ideal
                stop.flush()
            }) {
                if (liked) {
                    Icon(
                        painter = painterResource(R.drawable.heart_solid),
                        contentDescription = "Unlike",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.heart_regular),
                        contentDescription = "Like",
                        tint = colorResource(R.color.light_blue),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }, content)
    }

    @Composable
    fun Departures(departures: Departures, storage: IdStorage) {
        val stop = intent.getParcelableExtra<Stop>("stop")!!
        val context = LocalContext.current

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
    fun DeparturesShimmer(storage: IdStorage?) {
        val stop = intent.getParcelableExtra<Stop>("stop")!!
        val shimmer = rememberActivityShimmer()

        val entries: List<String?> = storage?.postStorage?.getPosts(stop)?.map { it.name } ?: listOf(null, null)

        LazyColumn {
            items(entries) { postName ->
                DepartureEntryShimmer(shimmer, postName = postName)
            }
        }
    }

    @Composable
    fun DepartureEntryShimmer(shimmer: Shimmer, postName: String?) {
        Container(
            innerPadding = 0.dp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                Column(Modifier.padding(bottom = 4.dp)) {
                    Crossfade(targetState = postName) { name ->
                        if (name != null) {
                            Text(
                                name,
                                color = colorResource(R.color.secondaryColor),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Box(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
                                ShimmerText(shimmer, height=18.dp, widthFraction = 0.4f, variance = 0.15f)
                            }
                        }
                    }
                    Divider(Modifier.padding(horizontal = 10.dp).padding(top = 4.dp))
                }

                repeat(departuresSettings.maxEntries) {
                    DepartureEntryRowShimmer(shimmer)
                }
            }
        }
    }



}