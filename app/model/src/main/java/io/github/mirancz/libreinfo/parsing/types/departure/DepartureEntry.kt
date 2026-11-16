package io.github.mirancz.libreinfo.parsing.types.departure

import io.github.mirancz.libreinfo.model.LineAlias

data class DepartureEntry(
    val line: LineAlias,
    val finalStop: String,
    val stopId: Short,
    val postId: Short,
    val lowFloor: Boolean,
    val time: DepartureTime,
    val tripId: Int?,
    val vehicleInfo: VehicleInfo
)
