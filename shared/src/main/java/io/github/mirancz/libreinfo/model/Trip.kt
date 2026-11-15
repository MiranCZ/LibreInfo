package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedByte
import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.codec.util.toCheckedUByte
import kotlinx.serialization.Serializable

/**
 * @see [StandaloneTrip]
 */
@Serializable
data class Trip(
    val tripId: Int,
    val lineId: Short,
    val serviceId: Short,
    val headsignId: Int,
    val blockId: Short,
    val lowFloor: Boolean,
    val startPos: Int,
    val routeLength: UByte
) {

    constructor(
        tripId: Int,
        lineId: Int,
        serviceId: Int,
        headsignId: Int,
        blockId: Int,
        lowFloor: Boolean,
        startPos: Int,
        routeLength: Int
    ) : this(
        tripId,
        lineId.toCheckedShort(),
        serviceId.toCheckedShort(),
        headsignId,
        blockId.toCheckedShort(),
        lowFloor,
        startPos,
        routeLength.toCheckedUByte()
    )

    fun intRouteLength(): Int {
        return routeLength.toInt()
    }

}
