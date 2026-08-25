package io.github.mirancz.libreinfo.parsing.types

import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import kotlin.collections.emptyList

data class VehicleInfo(
    val vehicleId: Int,
    val delay: Int,
    val lastStop: Stop,
    val lastObservedAt: Long,
    val lastModifiedAt: Long,
    val previousStops: List<PreviousStop>
) {

    companion object {
        val NONE: VehicleInfo = VehicleInfo(-1, -1, Stop.NONE, -1, -1, emptyList())
    }

}
