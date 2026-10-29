package io.github.mirancz.libreinfo.parsing.types.departure

import io.github.mirancz.libreinfo.parsing.types.LineAlias

data class DepartureEntry(
    val line: LineAlias,
    val finalStop: String,
    val stopId: Int,
    val postID: Int,
    val lowFloor: Boolean,
    val time: DepartureTime,
    val tripId: Int?,
    val vehicleInfo: VehicleInfo
)
