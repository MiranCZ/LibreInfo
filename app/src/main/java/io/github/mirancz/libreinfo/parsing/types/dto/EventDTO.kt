package io.github.mirancz.libreinfo.parsing.types.dto

import android.os.Parcelable
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.mirancz.libreinfo.parsing.storage.LineStorage
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.parsing.types.Diversion
import io.github.mirancz.libreinfo.parsing.types.Event
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class EventDTO(
    val id: Int?,
    val title: String,
    val content: String,
    val from: @Serializable(IsoDateTimeSerializer::class) DateTime = DateTime.NONE,
    val to: @Serializable(IsoDateTimeSerializer::class) DateTime = DateTime.NONE,
    val delay: Int?,
    val lines: List<Int>?
) {

    fun map(storage: IdStorage): Event =
        Event(id, title, content, from, to, delay, lines?.map { mapLine(storage, it) })


}


