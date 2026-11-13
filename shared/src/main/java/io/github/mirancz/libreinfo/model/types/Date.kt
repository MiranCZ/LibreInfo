package io.github.mirancz.libreinfo.model.types

import io.github.mirancz.libreinfo.codec.util.toCheckedByte
import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime



@Serializable
data class Date(
    val day: Byte,
    val month: Byte,
    val year: Short,
    val dayOfWeek: DayOfWeek
) {

    constructor(day: Int, month: Int, year: Int) : this(
        day.toCheckedByte(), month.toCheckedByte(), year.toCheckedShort()
    )

    constructor(day: Byte, month: Byte, year: Short) : this(
        day, month, year, LocalDate.of(year.toInt(), month.toInt(), day.toInt()).getDayOfWeek()
    )

    companion object {

        @JvmStatic
        fun now(): Date {
            val now = LocalDateTime.now()

            return Date(now.dayOfMonth, now.monthValue, now.year)
        }

    }

    fun isAfter(date: Date): Boolean {
        if (year > date.year) return true
        else if (year < date.year) return false

        if (month > date.month) return true
        else if (month < date.month) return false

        return day >= date.day
    }

    fun isBefore(date: Date): Boolean {
        if (year < date.year) return true
        else if (year > date.year) return false

        if (month < date.month) return true
        else if (month > date.month) return false

        return day <= date.day
    }

    fun isBetween(from: Date, to: Date) =
        isAfter(from) && isBefore(to)

}
