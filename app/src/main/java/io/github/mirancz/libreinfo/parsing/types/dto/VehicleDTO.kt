package io.github.mirancz.libreinfo.parsing.types.dto

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.Vehicle
import kotlinx.serialization.Serializable

@Serializable
data class VehicleDTO(
    val id: Int,
    val connectedIds: List<Int>?,
    val vehicleType: VehicleType?,
    val lineType: VehicleType?,
    val latitude: Double?,
    val longitude: Double?,
    val bearing: Int?,
    val lineId: Int,
    val routeId: Int,
    val serviceId: Int?,
    val course: String?,
    val lowFloor: Boolean?,
    val delay: Int?,
    val lastStopId: Int,
    val finalStopId: Int,
    val finalDestinationName: String?,
    val inactive: Boolean?
) {
    fun map(storage: IdStorage): Vehicle {
        return Vehicle(
            id,
            connectedIds,
            vehicleType,
            lineType,
            latitude,
            longitude,
            bearing,
            mapLine(storage, lineId),
            routeId,
            serviceId,
            course,
            lowFloor,
            delay,
            mapStop(storage, lastStopId),
            mapStop(storage, finalStopId),
            finalDestinationName,
            inactive
        )
    }
}
