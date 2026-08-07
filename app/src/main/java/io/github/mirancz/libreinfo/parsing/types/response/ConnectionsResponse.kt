package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.connection.Connection
import io.github.mirancz.libreinfo.parsing.types.dto.connection.ConnectionDTO
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionsResponse(val connections: List<ConnectionDTO>)
