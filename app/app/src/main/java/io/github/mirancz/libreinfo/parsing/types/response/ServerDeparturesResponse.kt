package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.dto.DeparturesMetadata
import io.github.mirancz.libreinfo.parsing.types.dto.ServerPostDTO
import kotlinx.serialization.Serializable

@Serializable
data class ServerDeparturesResponse(
    val stopId: Int = -1,
    val message: String?,
    val posts: List<ServerPostDTO> = emptyList(),
    val metadata: DeparturesMetadata?
)
