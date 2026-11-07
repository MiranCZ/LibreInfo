package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import kotlinx.serialization.Serializable

@Serializable
data class ApiEntry(
    val tripId: Int,
    val lineId: Short,
    val routeId: Short
) {

    constructor(tripId: Int, lineId: Int, routeId: Int) : this(tripId, lineId.toCheckedShort(), routeId.toCheckedShort())


}
