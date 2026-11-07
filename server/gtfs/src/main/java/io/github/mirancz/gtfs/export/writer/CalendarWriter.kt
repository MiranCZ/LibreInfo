package io.github.mirancz.gtfs.export.writer

import io.github.mirancz.gtfs.ParsedGtfs
import io.github.mirancz.gtfs.export.Writer
import io.github.mirancz.libreinfo.codec.CalendarCodec
import io.github.mirancz.libreinfo.codec.CalendarDatesCodec
import io.github.mirancz.libreinfo.codec.TransitDataCodec
import io.github.mirancz.libreinfo.codec.util.CheckedOutputStream
import io.github.mirancz.libreinfo.model.CalendarDate
import io.github.mirancz.libreinfo.model.CalendarEntry
import io.github.mirancz.libreinfo.model.types.Date
import java.io.IOException
import kotlin.experimental.or

class CalendarWriter : Writer {

    override fun write(gtfs: ParsedGtfs, creator: TransitDataCodec.StreamCreator) {
        try {
            val os = creator.create("calendar")
            this.writeCalendar(gtfs, os)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        try {
            val os = creator.create("calendar_dates")
            this.writeCalendarDates(gtfs, os)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun writeCalendar(gtfs: ParsedGtfs, os: CheckedOutputStream) {
        val calendar = gtfs.calendar

        val mapped = calendar.map { c ->
            var data: Byte = 0
            if (c.monday.toInt() == 1) data = data or (1 shl 0).toByte()
            if (c.tuesday.toInt() == 1) data = data or (1 shl 1).toByte()
            if (c.wednesday.toInt() == 1) data = data or (1 shl 2).toByte()
            if (c.thursday.toInt() == 1) data = data or (1 shl 3).toByte()
            if (c.friday.toInt() == 1) data = data or (1 shl 4).toByte()
            if (c.saturday.toInt() == 1) data = data or (1 shl 5).toByte()
            if (c.sunday.toInt() == 1) data = data or (1 shl 6).toByte()

            CalendarEntry(
                c.service_id,
                data,
                parseDate(c.start_date),
                parseDate(c.end_date)
            )
        }

        CalendarCodec.write(os, mapped)
    }

    private fun writeCalendarDates(gtfs: ParsedGtfs, os: CheckedOutputStream) {
        val calendarDates = gtfs.calendarDates

        CalendarDatesCodec.write(os, calendarDates.map { c ->
            CalendarDate(
                c.service_id,
                parseDate(c.date),
                c.exception_type
            )
        })
    }

    private fun parseDate(date: String): Date {
        val day = date.substring(date.length - 2).toInt()
        val month = date.substring(date.length - 4, date.length - 2).toInt()

        val year = date.substring(0, 4).toInt()

        return Date(day.toByte(), month.toByte(), year.toShort())
    }


}