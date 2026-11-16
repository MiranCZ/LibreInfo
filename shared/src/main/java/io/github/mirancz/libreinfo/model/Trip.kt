package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.codec.util.toCheckedUByte
import kotlinx.serialization.Serializable

/**
 * Data of a single [GTFS trip](https://gtfs.org/documentation/schedule/reference/#tripstxt).
 *
 * @see TripSchedule
 */
@Serializable
data class Trip(
    val tripId: Int,
    val lineId: Short,
    val serviceId: Short,
    val headsign: String,
    val blockId: Short,
    val lowFloor: Boolean,
    val startPos: Int,
    val routeLength: UByte
) {

    constructor(
        tripId: Int,
        lineId: Int,
        serviceId: Int,
        headsign: String,
        blockId: Int,
        lowFloor: Boolean,
        startPos: Int,
        routeLength: Int
    ) : this(
        tripId,
        lineId.toCheckedShort(),
        serviceId.toCheckedShort(),
        headsign,
        blockId.toCheckedShort(),
        lowFloor,
        startPos,
        routeLength.toCheckedUByte()
    )

    fun intRouteLength(): Int {
        return routeLength.toInt()
    }

}
