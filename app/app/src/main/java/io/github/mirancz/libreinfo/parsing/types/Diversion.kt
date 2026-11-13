package io.github.mirancz.libreinfo.parsing.types

import io.github.mirancz.libreinfo.model.LineAlias
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import kotlinx.serialization.Serializable

/**
 * This class is not [Serializable], see [io.github.mirancz.libreinfo.parsing.types.dto.DiversionDTO] instead
 */
@Serializable
data class Diversion(
    val id: Int?,
    val title: String,
    val content: String,
    val from: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val to: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val lines: List<LineAlias>?
)



