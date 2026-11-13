package io.github.mirancz.libreinfo.codec

import io.github.mirancz.libreinfo.codec.util.AppInputStream
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.CalendarEntry
import io.github.mirancz.libreinfo.model.Post
import java.io.IOException

object CalendarCodec {

    @JvmStatic
    @Throws(IOException::class)
    fun write(os: CheckedOutputStream, calendar: List<CalendarEntry>) {
        os.writeInt(calendar.size)

        for (e in calendar) {
            os.writeShort(e.serviceId)
            os.writeByte(e.dayBits)
            os.writeDate(e.from)
            os.writeDate(e.to)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun read(inp: AppInputStream, consumer: (CalendarEntry) -> Unit) {
        val length = inp.readInt()

        for (i in 0 until length) {
            consumer(CalendarEntry(
                inp.readShort(),
                inp.readByte(),
                inp.readDate(),
                inp.readDate()
            ))
        }
    }

}