package io.github.mirancz.libreinfo.parsing.types

import android.os.Parcelable
import io.github.mirancz.libreinfo.parsing.types.serial.DateTimeParceler
import io.github.mirancz.libreinfo.parsing.types.serial.LineAliasParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * This class is not [Serializable], see [io.github.mirancz.libreinfo.parsing.types.dto.DiversionDTO] instead
 */
@Parcelize
@TypeParceler<DateTime, DateTimeParceler>()
@TypeParceler<LineAlias, LineAliasParceler>()
data class Diversion(
    val id: Int?,
    val title: String,
    val content: String,
    val from: DateTime,
    val to: DateTime,
    val lines: List<LineAlias>?
) : Parcelable {

}


