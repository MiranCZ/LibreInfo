package me.miran.mhdstuff.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.miran.mhdstuff.R
import me.miran.mhdstuff.activity.base.KBaseActivity
import me.miran.mhdstuff.exception.RequestException
import me.miran.mhdstuff.parsing.storage.IdStorage
import me.miran.mhdstuff.parsing.types.Vehicle
import me.miran.mhdstuff.util.DelayUtil
import me.miran.mhdstuff.util.Either
import me.miran.mhdstuff.util.request.RequestHelper

class VehiclesListActivity : KBaseActivity(R.string.vehicles) {

    @Composable
    override fun CreateElements() {
        var vehiclesResult by remember { mutableStateOf(Either.left<List<Vehicle>?, RequestException>(null)) }

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val result = withContext(Dispatchers.IO) {
                val storage = IdStorage.getInstance();

                try {
                    Either.left<List<Vehicle>, RequestException>(Vehicle.parseVehicles(RequestHelper.getVehicles(context), storage))
                } catch (e: RequestException) {
                    // TODO handle exception
                    Either.right(e)
                }
            }

            vehiclesResult = result
        }

        when (val local = vehiclesResult) {
            is Either.Left -> {
                var vehicles = local.left;

                if (vehicles != null) {
                    vehicles = vehicles.sortedBy { vehicle -> vehicle.line.id }

                    if (vehicles.isEmpty()) {
                        NothingHere()
                    } else {
                        LazyColumn {
                            items(vehicles) { vehicle ->
                                VehicleEntry(vehicle)
                            }
                        }
                    }
                } else {
                    Loading()
                }
            }
            is Either.Right -> {
                val error = local.right;

                ErrorWidget(error)
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