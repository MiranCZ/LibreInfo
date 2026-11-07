package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.model.types.Date
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEntry(
    val serviceId: Short, val dayBits: Byte, val from: Date, val to: Date
) {

    constructor(serviceId: Int, dayBits: Byte, from: Date, to: Date) : this(
        serviceId.toCheckedShort(), dayBits, from, to
    )

}
