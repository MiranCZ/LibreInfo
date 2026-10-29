package io.github.mirancz.libreinfo.parsing.types

import io.github.mirancz.libreinfo.parsing.types.stop.Stop

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
) {

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

}