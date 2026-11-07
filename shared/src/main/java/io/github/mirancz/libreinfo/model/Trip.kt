package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedByte
import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.codec.util.toCheckedUByte
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val routeId: Short,
    val serviceId: Short,
    val headsign: String,
    val blockId: Short,
    val bikesAllowed: Byte,
    val routeLength: UByte
) {

    constructor(
        routeId: Int,
        serviceId: Int,
        headsign: String,
        blockId: Int,
        bikesAllowed: Int,
        routeLength: Int
    ) : this(
        routeId.toCheckedShort(),
        serviceId.toCheckedShort(),
        headsign,
        blockId.toCheckedShort(),
        bikesAllowed.toCheckedByte(),
        routeLength.toCheckedUByte()
    )

}
