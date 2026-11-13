package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.model.types.Date
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
data class CalendarEntry(
    val serviceId: Short, val dayBits: Byte, val from: Date, val to: Date
) {

    constructor(serviceId: Int, dayBits: Byte, from: Date, to: Date) : this(
        serviceId.toCheckedShort(), dayBits, from, to
    )

    fun availableOn(date: Date): Boolean {
        if (!date.isBetween(from, to)) return false

        val day = date.dayOfWeek

        return dayBits.toInt().shr(day.ordinal).and(1) == 1;
    }

}
