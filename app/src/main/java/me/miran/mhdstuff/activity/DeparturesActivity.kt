package me.miran.mhdstuff.activity

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.activity.data.DelaysDataHolder
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.departure.DepartureEntry
import me.miran.mhdstuff.parsing.types.departure.Departures
import me.miran.mhdstuff.parsing.types.stop.Stop
import me.miran.mhdstuff.util.OfflineDepartures
import me.miran.mhdstuff.util.Text


class DeparturesActivity : KBaseActivity("") {

    class StopViewModel : ViewModel() {
        private val _liked = mutableStateOf(false)
        val liked = _liked

        fun toggleLiked() {
            _liked.value = !_liked.value
        }

        fun setLiked(value: Boolean) {
            _liked.value = value
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

        val delays = DelaysDataHolder.getDelays()
        Thread {
            val storage = IdStorage.getInstance()
            val departures = Departures(
                "Work in progress...",
                OfflineDepartures.getOffline(storage, stop.id.internal, delays)
            )

            runOnUiThread {
                setBaseContent {
                    this.Departures(departures, storage)
                }
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

        LazyColumn {
            items(departures.departures) { entry ->

                val post = storage.postStorage.getPost(stop.id.internal, entry.postID)

                Container(
                    {
                        startActivity(
                            DeparturePostDetailActivity::class
                        ) { intent -> intent.putExtra("post", post) }
                    },
                    innerPadding = 0.dp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                        androidx.compose.material3.Text(
                            entry.name,
                            color = colorResource(R.color.secondaryColor),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp)
                        )

                        Divider(Modifier.padding(vertical = 4.dp, horizontal = 10.dp))

                        for (dep in entry.entries) {
                            DepartureEntry(dep)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DepartureEntry(departure: DepartureEntry) {
        val vehicleInfo = departure.vehicleInfo
        val showDelay = true

        Box(
            Modifier
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(null, ripple(color = Color.White), onClick = {
                    startActivity(
                        TripDetailActivity::class
                    ) { intent: Intent ->
                        if (vehicleInfo.hasDelay()) {
                            intent.putExtra("delay", vehicleInfo.delay())
                        }
                        if (vehicleInfo.hasId()) {
                            intent.putExtra("vehicleId", vehicleInfo.id())
                        }

                        intent.putExtra("stopId", departure.stopId)
                        intent.putExtra("tripId", departure.tripId)
                    }
                })
                .padding(horizontal = 8.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Row(Modifier.weight(3f)) {
                    LineIcon(departure.line)
                    androidx.compose.material3.Text(
                        departure.finalStop,
                        fontSize = 14.sp,
                        color = colorResource(R.color.secondaryColor),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 4.dp)
                    )
                }

                if (departure.lowFloor) {
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
                    if (vehicleInfo.hasDelay() && showDelay) {
                        val delay: Int = vehicleInfo.delay()
                        val color: Int = vehicleInfo.delayColor

                        departure.timeMark.stopTime.delay = delay
                        val arrivalText: String = departure.timeMark.getFormattedString(30, true)

                        var delayStr = ""
                        if (delay > 0) {
                            delayStr = " ($delay) "
                        }

                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.Text(
                            delayStr + arrivalText,
                            color = Color(color),
                            fontSize = 14.sp
                        )
                    } else {
                        val arrivalText: String = departure.timeMark.getFormattedString(30, false)

                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.Text(text = arrivalText, fontSize = 14.sp)
                    }
                }
            }
        }
    }

}