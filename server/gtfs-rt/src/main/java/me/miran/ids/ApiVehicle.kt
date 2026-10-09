package me.miran.ids;

import me.miran.model.Vehicle
import me.miran.util.IdStorage

data class ApiVehicle(
    val id: Int?,
    val idB: Int?,
    val idC: Int?,
    val vehicleType: String?,
    val lineType: String?,
    val latitude: Double?,
    val longitude: Double?,
    val bearing: Int?,
    val lineId: Int?,
    val routeId: Int?,
    val serviceId: Int?,
    val course: String?,
    val isLowFloor: Boolean?,
    val delay: Int?,
    val lastStopId: Int?,
    val finalStopId: Int?,
    val finalDestinationName: String?,
    val isInactive: Boolean?,
    val driverName: String?
) {

    fun mapIds(): ApiVehicle? {
        val stopMap = { i: Int? -> IdStorage.STOP.getId(i) }

        return ApiVehicle(
            id,
            idB,
            idC,
            vehicleType,
            lineType,
            latitude,
            longitude,
            bearing,
            lineId,
            routeId,
            serviceId, // THIS IS NOT THE SAME AS GTFS's `serviceId` and should not be mapped!
            course,
            isLowFloor,
            delay,
            stopMap(lastStopId).orElse { return null },
            stopMap(finalStopId).orElse { return null },
            finalDestinationName,
            isInactive,
            driverName
        )
    }

    fun map(): Vehicle {
        if (id == null || lineId == null || routeId == null || lastStopId == null || finalStopId == null) {
            throw NullPointerException("Null parameter in ApiVehicle")
        }

        val connectedIds = ArrayList<Int>()
        if (idB != null && idB != 0) connectedIds.add(idB);
        if (idC != null && idC != 0) connectedIds.add(idC);

        return Vehicle(
            id,
            connectedIds,
            vehicleType,
            lineType,
            latitude,
            longitude,
            bearing,
            lineId,
            routeId,
            serviceId,
            course,
            isLowFloor,
            delay,
            lastStopId,
            finalStopId,
            finalDestinationName,
            isInactive
        );
    }

}
