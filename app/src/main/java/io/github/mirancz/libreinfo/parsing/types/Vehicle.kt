package io.github.mirancz.libreinfo.parsing.types

import android.content.Context
import android.os.Parcelable
import android.text.SpannableString
import io.github.mirancz.libreinfo.parsing.types.dto.VehicleType
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.util.DelayUtil
import kotlinx.parcelize.Parcelize
import java.util.function.Supplier

@Parcelize
data class Vehicle(
    val id: Int,
    val connectedIds: List<Int>?,
    val vehicleType: VehicleType?,
    val lineType: VehicleType?,
    val latitude: Double?,
    val longitude: Double?,
    val bearing: Int?,
    val line: LineAlias,
    val routeId: Int,
    val serviceId: Int?,
    val course: String?,
    val lowFloor: Boolean?,
    val delay: Int?,
    val lastStop: Stop,
    val finalStop: Stop,
    val finalDestinationName: String?,
    val inactive: Boolean?
) : Parcelable {

    fun getVehicleNumbersString(): String {
        var res = id.toString() + ""

        if (connectedIds != null) {
            for (id in connectedIds) {
                res = "$res + $id"
            }
        }

        return res
    }

    fun getFinalStopText(): String {
        if (finalDestinationName != null) return finalDestinationName

        return finalStop.name
    }

    fun getDelaySpan(context: Context?): SpannableString {
        return DelayUtil.getDelaySpan(context, delay!!)
    }

    fun getDelayColor(): Int {
        return DelayUtil.getDelayColor(delay!!)
    }

}
