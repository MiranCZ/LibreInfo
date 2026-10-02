package io.github.mirancz.libreinfo.parsing.types

import android.os.Parcelable
import io.github.mirancz.libreinfo.parsing.types.serial.DateTimeParceler
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import io.github.mirancz.libreinfo.parsing.types.serial.LineAliasParceler
import io.github.mirancz.libreinfo.parsing.types.serial.LineAliasSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

/**
 * This class is not [Serializable], see [io.github.mirancz.libreinfo.parsing.types.dto.DiversionDTO] instead
 */
@Serializable
@Parcelize
@TypeParceler<DateTime, DateTimeParceler>()
@TypeParceler<LineAlias, LineAliasParceler>()
data class Diversion(
    val id: Int?,
    val title: String,
    val content: String,
    val from: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val to: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val lines: List<@Serializable(LineAliasSerializer::class) LineAlias>?
) : Parcelable {

}


