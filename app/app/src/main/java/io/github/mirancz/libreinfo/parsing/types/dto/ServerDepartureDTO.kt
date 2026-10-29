package io.github.mirancz.libreinfo.parsing.types.dto

import kotlinx.serialization.Serializable

@Serializable
data class ServerDepartureDTO(
    val lineId: Int = -1,
    val routeId: Int = -1,
    val finalStop: String = "",
    val isLowFloor: Boolean = false,
    val platform: String?,
    val time: String = ""
)

@Serializable
data class ServerPostDTO(
    val postId: Int = -1,
    val name: String = "",
    val departures: List<ServerDepartureDTO> = emptyList()
)

@Serializable
data class DeparturesMetadata(
    val language: String?,
    val fromCache: Boolean = false,
    val error: String?
)
