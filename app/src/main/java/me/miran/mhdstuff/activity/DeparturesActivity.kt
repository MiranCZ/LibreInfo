package me.miran.mhdstuff.activity

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.JsonObject
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.activity.data.DelaysDataHolder
import me.miran.mhdstuff.exception.RequestException
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.departure.Departures
import me.miran.mhdstuff.parsing.types.stop.Stop
import me.miran.mhdstuff.util.OfflineDepartures
import me.miran.mhdstuff.util.Text
import me.miran.mhdstuff.util.request.RequestHelper


class DeparturesActivity : KBaseActivity("") {

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

        LaunchedEffect(Unit) {
            if (stop.isFavourite) {
                vm.setLiked(true)
            }
        }

        createDepartures(stop)
    }

    fun createDepartures(stop: Stop, refreshDelays: Boolean = false ,onFinish: () -> Unit = {}) {
        var delays = DelaysDataHolder.getDelays()
        Thread {
            if (refreshDelays) {
                try {
                    delays = RequestHelper.getRouteDelays(this)
                } catch (e: RequestException) {
                    // TODO show error
//                e.showError(this, AppException.NotificationType.SNACK_BAR)
                    delays = JsonObject()
                }
            }

            val storage = IdStorage.getInstance()
            val departures = Departures(
                "Work in progress...",
                OfflineDepartures.getOffline(storage, stop.id.internal, delays)
            )

            runOnUiThread {
                setBaseContent {
                    this.Departures(departures, storage)
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

        val vm: StopViewModel = viewModel()
        val refreshing by vm.refreshing

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