package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.CalendarDate
import io.github.mirancz.libreinfo.model.CalendarEntry
import java.io.IOException

object CalendarDatesCodec {


    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, calendarDates: List<CalendarDate>) {
        os.writeInt(calendarDates.size)

        for (e in calendarDates) {
            os.writeShort(e.serviceId)
            os.writeDate(e.date)
            os.writeByte(e.exceptionType)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream): List<CalendarDate> {
        val result = ArrayList<CalendarDate>()

        read(inp) { result.add(it) }

        return result
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream, consumer: (CalendarDate) -> Unit) {
        val length = inp.readInt()

        for (i in 0 until length) {
            consumer(CalendarDate(
                inp.readShort(),
                inp.readDate(),
                inp.readByte()
            ))
        }
    }
}