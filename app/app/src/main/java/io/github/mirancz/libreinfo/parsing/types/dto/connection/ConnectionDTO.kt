package io.github.mirancz.libreinfo.parsing.types.dto.connection

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.parsing.types.connection.Connection
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionDTO(
    val departure: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val arrival: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val duration: Int,
    val transfers: Int,
    val legs: List<ConnectionLegDTO>
) {

    fun map(storage: IdStorage): Connection {
        return Connection(departure, arrival, duration, transfers, legs.map { it.map(storage) })
    }

}
