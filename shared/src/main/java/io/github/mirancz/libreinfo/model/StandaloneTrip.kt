package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedByte
import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.codec.util.toCheckedUByte
import kotlinx.serialization.Serializable

/**
 * Unlike [Trip] this includes the [headsign] as a [String] instead of an [Int] id
 *
 *
 * @see Trip
 */
@Serializable
data class StandaloneTrip(
    val lineId: Short,
    val serviceId: Short,
    val headsign: String,
    val blockId: Short,
    val lowFloor: Byte,
    val routeLength: UByte
) {

    constructor(
        lineId: Int,
        serviceId: Int,
        headsign: String,
        blockId: Int,
        lowFloor: Int,
        routeLength: Int
    ) : this(
        lineId.toCheckedShort(),
        serviceId.toCheckedShort(),
        headsign,
        blockId.toCheckedShort(),
        lowFloor.toCheckedByte(),
        routeLength.toCheckedUByte()
    )

}
