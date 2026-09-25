package io.github.mirancz.libreinfo.parsing.types

import android.os.Parcelable
import io.github.mirancz.libreinfo.parsing.types.serial.DateTimeParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler


/**
 * This class is not [Serializable], see [io.github.mirancz.libreinfo.parsing.types.dto.EventDTO] instead
 */
@Parcelize
@TypeParceler<DateTime, DateTimeParceler>()
data class Event(
    val id: Int?,
    val title: String,
    val content: String,
    val from: DateTime,
    val to: DateTime,
    val delay: String?,
    val lines: List<LineAlias>?
) : Parcelable
