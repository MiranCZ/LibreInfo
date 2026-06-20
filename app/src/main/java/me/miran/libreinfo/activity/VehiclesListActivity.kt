package me.miran.libreinfo.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.Shimmer
import me.miran.libreinfo.R
import me.miran.libreinfo.activity.base.KBaseActivity
import me.miran.libreinfo.parsing.storage.manager.AppContainer
import me.miran.libreinfo.parsing.storage.manager.IdStorage
import me.miran.libreinfo.parsing.types.Vehicle
import me.miran.libreinfo.util.DelayUtil
import me.miran.libreinfo.util.load.rememberLoad
import me.miran.libreinfo.util.request.RequestHelper

class VehiclesListActivity : KBaseActivity(R.string.vehicles) {

    @Composable
    override fun CreateElements() {
        val context = LocalContext.current

        val vehicles = rememberLoad {
            val storage = AppContainer.storageProvider.getInstance()
            Vehicle.parseVehicles(RequestHelper.getVehicles(context), storage)
                .sortedBy { vehicle -> vehicle.line.id }
        }

        AsyncContent(vehicles, loading = { VehicleListShimmer() }) { vehicleList ->
            if (vehicleList.isEmpty()) {
                NothingHere()
            } else {
                LazyColumn {
                    items(vehicleList) { vehicle ->
                        VehicleEntry(vehicle)
                    }
                }
            }
        }
    }

    @Composable
    fun VehicleListShimmer() {
        val shimmer = rememberActivityShimmer()
        LazyColumn {
            items(6) { VehicleEntryShimmer(shimmer) }
        }
    }

    @Composable
    fun VehicleEntryShimmer(shimmer: Shimmer) {
        Container(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Column {
                Row(Modifier.fillMaxWidth()) {
                    ShimmerBox(Modifier.weight(1f).height(14.dp), shimmer)
                    Spacer(Modifier.width(8.dp))
                    ShimmerBox(Modifier.weight(2f).height(14.dp), shimmer)
                    Spacer(Modifier.width(8.dp))
                    ShimmerBox(Modifier.width(40.dp).height(14.dp), shimmer)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    ShimmerLineIcon(shimmer)
                    Spacer(Modifier.width(12.dp))
                    ShimmerBox(Modifier.weight(1f).height(20.dp), shimmer)
                }
                Spacer(Modifier.height(8.dp))
                ShimmerBox(Modifier.fillMaxWidth(0.4f).height(14.dp), shimmer)
            }
        }
    }

    @Composable
    fun VehicleEntry(item: Vehicle) {
        val context = LocalContext.current

        Container(
            onClick = {
                // TODO add on-click
            },
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column() {
                Row(Modifier.fillMaxWidth()) {
                    Row(Modifier.weight(1f)) {
                        Text(item.vehicleNumbersString, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.weight(2f)) {
                        Text(item.serviceId, fontSize = 14.sp, fontWeight = FontWeight.Light)
                    }

                    Text(DelayUtil.getDelayText(context, item.delay), fontSize = 14.sp, color = Color(DelayUtil.getDelayColor(item.delay)))
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
                    Row(Modifier.weight(1f)) {
                        LineIcon(item.line, padding = 0.dp)
                        Text(
                            item.finalStopText,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 12.dp)
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.wheelchair_regular),
                        "lowfloor",
                        Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically),
                        tint = colorResource(R.color.secondary_color_light_tone)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.lastStop.name, color = colorResource(R.color.secondary_color_tone), fontSize = 14.sp)
                }
            }
        }
    }


}