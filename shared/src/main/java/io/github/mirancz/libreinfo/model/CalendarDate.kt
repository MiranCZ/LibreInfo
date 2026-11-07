package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedByte
import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.model.types.Date
import kotlinx.serialization.Serializable

@Serializable
data class CalendarDate(
    val serviceId: Short,
    val date: Date,
    val exceptionType: Byte
) {

    constructor(serviceId: Int, date: Date, exceptionType: Int) : this(
        serviceId.toCheckedShort(),
        date,
        exceptionType.toCheckedByte()
    )

}
