package io.github.mirancz.libreinfo.parsing.types.serial

import android.os.Parcel
import io.github.mirancz.libreinfo.parsing.types.LineAlias
import kotlinx.parcelize.Parceler


object LineAliasParceler : Parceler<LineAlias> {

    override fun LineAlias.write(
        parcel: Parcel,
        flags: Int
    ) {
        parcel.writeInt(id)
        parcel.writeString(lineDisplayName)
        parcel.writeInt(backgroundColor)
        parcel.writeString(backgroundColorStr)
        parcel.writeInt(textColor)
        parcel.writeString(textColorStr)
    }

    override fun create(parcel: Parcel): LineAlias {
        return LineAlias(parcel.readInt(), parcel.readString(), parcel.readInt(), parcel.readString(), parcel.readInt(), parcel.readString())
    }


}