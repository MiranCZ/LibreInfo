package io.github.mirancz.libreinfo.parsing.types

import io.github.mirancz.libreinfo.model.LineAlias

/**
 * This class is not [Serializable], see [io.github.mirancz.libreinfo.parsing.types.dto.EventDTO] instead
 */
data class Event(
    val id: Int?,
    val title: String,
    val content: String,
    val from: DateTime,
    val to: DateTime,
    val delay: String?,
    val lines: List<LineAlias>?
)