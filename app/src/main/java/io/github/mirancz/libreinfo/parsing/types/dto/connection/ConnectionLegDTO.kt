package io.github.mirancz.libreinfo.parsing.types.dto.connection

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.parsing.types.connection.ConnectionLeg
import io.github.mirancz.libreinfo.parsing.types.connection.TransportMode
import io.github.mirancz.libreinfo.parsing.types.dto.mapStop
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionLegDTO(
    val departure: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val arrival: @Serializable(IsoDateTimeSerializer::class) DateTime,
    val fromId: Int,
    val toId: Int,
    val tripId: Int?,
    val duration: Int,
    val mode: String,
    val geometry: String,
    val delay: Int,
    val realtime: Boolean,
    val distance: Double?
) {

    fun map(storage: IdStorage): ConnectionLeg {
        val transport: TransportMode

        if (tripId != null) {
            transport = TransportMode.vehicle(tripId)
        } else {
            transport = TransportMode.walk((distance ?: -1).toInt())
        }

        return ConnectionLeg(
            departure,
            arrival,
            mapStop(storage, fromId),
            mapStop(storage, toId),
            duration,
            mode,
            geometry,
            delay,
            realtime,
            transport
        )
    }

}