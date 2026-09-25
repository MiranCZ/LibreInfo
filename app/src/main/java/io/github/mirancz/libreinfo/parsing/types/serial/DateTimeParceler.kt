package io.github.mirancz.libreinfo.parsing.types.serial

import android.os.Parcel
import io.github.mirancz.libreinfo.parsing.types.DateTime
import kotlinx.parcelize.Parceler

/**
 * Parcels [DateTime] for `@Parcelize` classes, since [DateTime] itself lives in the Android-free
 * model module. Use with `@TypeParceler<DateTime, DateTimeParceler>()`.
 */
object DateTimeParceler : Parceler<DateTime> {

    override fun create(parcel: Parcel): DateTime {
        if (parcel.readByte() == 0.toByte()) return DateTime.NONE

        return DateTime(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt())
    }

    override fun DateTime.write(parcel: Parcel, flags: Int) {
        if (this == DateTime.NONE) {
            parcel.writeByte(0)
            return
        }
        parcel.writeByte(1)

        parcel.writeInt(day)
        parcel.writeInt(month)
        parcel.writeInt(year)
        parcel.writeInt(hours)
        parcel.writeInt(minutes)
    }
}

/** [DateTimeParceler] for `DateTime?` properties. Use with `@TypeParceler<DateTime?, NullableDateTimeParceler>()`. */
object NullableDateTimeParceler : Parceler<DateTime?> {

    override fun create(parcel: Parcel): DateTime? {
        if (parcel.readByte() == 0.toByte()) return null

        return with(DateTimeParceler) { create(parcel) }
    }

    override fun DateTime?.write(parcel: Parcel, flags: Int) {
        if (this == null) {
            parcel.writeByte(0)
            return
        }
        parcel.writeByte(1)

        val value: DateTime = this
        with(DateTimeParceler) { value.write(parcel, flags) }
    }
}
