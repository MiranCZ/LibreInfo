package me.miran.model

data class Vehicle(
    val id: Int,
    val connectedIds: List<Int>?,
    val vehicleType: String?,
    val lineType: String?,
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
)
