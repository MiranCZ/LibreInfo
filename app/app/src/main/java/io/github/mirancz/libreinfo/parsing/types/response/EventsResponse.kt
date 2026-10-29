package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.dto.EventDTO
import kotlinx.serialization.Serializable

@Serializable
data class EventsResponse(val events: List<EventDTO>)
