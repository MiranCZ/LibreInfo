package me.miran.libreinfo.activity

import java.time.temporal.ChronoUnit
import me.miran.libreinfo.parsing.storage.manager.IdStorage
import me.miran.libreinfo.parsing.types.DateTime
import me.miran.libreinfo.parsing.types.LineAlias
import me.miran.libreinfo.parsing.types.Trip
import me.miran.libreinfo.parsing.types.connection.Connection
import me.miran.libreinfo.parsing.types.connection.ConnectionPart

internal enum class WalkKind { TO_STOP, TRANSFER, TO_DESTINATION }

internal sealed interface LegUi

internal data class WalkLegUi(
    val kind: WalkKind,
    val durationMin: Int,
    val distance: Int,
) : LegUi

internal data class VehicleLegUi(
    val alias: LineAlias,
    val headsign: String,
    val stopCount: Int,
    val boardTime: String,
    val boardStop: String,
    val alightTime: String,
    val alightStop: String,
    val tripId: Int,
    val boardStopInternalId: Int,
) : LegUi

internal data class ConnectionUi(
    val depTime: String,
    val arrTime: String,
    val durationMin: Int,
    val countdownMin: Int,
    val legs: List<LegUi>,
)

internal fun buildConnectionUi(connection: Connection, storage: IdStorage, now: DateTime): ConnectionUi {
    val parts = connection.parts
    val legs = parts.mapIndexed { i, part -> buildLeg(part, i, parts, storage) }

    return ConnectionUi(
        depTime = connection.departure.toTimeString(),
        arrTime = connection.arrival.toTimeString(),
        durationMin = minutesBetween(connection.departure, connection.arrival),
        countdownMin = minutesUntil(connection.departure, now),
        legs = legs,
    )
}

private fun buildLeg(part: ConnectionPart, index: Int, parts: List<ConnectionPart>, storage: IdStorage): LegUi {
    val transport = part.transport
    if (transport.isVehicle) {
        val tripId = transport.tripId
        val lineId = storage.apiStorage.getLineIdAndRoute(tripId).left
        val alias = storage.lineStorage.getAlias(lineId)
        val trip = storage.tripStorage.trips[tripId]
        return VehicleLegUi(
            alias = alias,
            headsign = storage.tripStorage.getTripHeadsign(trip),
            stopCount = computeStopCount(trip, storage, part.fromStop.id.internal, part.toStop.id.internal),
            boardTime = part.departure.toTimeString(),
            boardStop = part.fromStop.name,
            alightTime = part.arrival.toTimeString(),
            alightStop = part.toStop.name,
            tripId = tripId,
            boardStopInternalId = part.fromStop.id.internal,
        )
    }

    val kind = when {
        parts.size == 1 -> WalkKind.TO_DESTINATION
        index == 0 -> WalkKind.TO_STOP
        index == parts.lastIndex -> WalkKind.TO_DESTINATION
        else -> WalkKind.TRANSFER
    }
    return WalkLegUi(
        kind = kind,
        durationMin = minutesBetween(part.departure, part.arrival),
        distance = transport.getDistance(),
    )
}

private fun computeStopCount(trip: Trip, storage: IdStorage, fromInternal: Int, toInternal: Int): Int {
    val routeStops = trip.getRouteStops(storage.routeStopStorage)
    val fromIdx = routeStops.indexOfFirst { it.stopId.toInt() == fromInternal }
    val toIdx = routeStops.indexOfLast { it.stopId.toInt() == toInternal }
    return if (fromIdx >= 0 && toIdx > fromIdx) toIdx - fromIdx else 0
}

internal fun minutesBetween(from: DateTime, to: DateTime): Int =
    ChronoUnit.MINUTES.between(from.toLocalDateTime(), to.toLocalDateTime()).toInt()

internal fun minutesUntil(target: DateTime, now: DateTime): Int =
    ChronoUnit.MINUTES.between(now.toLocalDateTime(), target.toLocalDateTime()).toInt()

internal fun formatMinutes(minutes: Int): String {
    return if (minutes >= 60) "${minutes / 60} h ${minutes % 60} min" else "$minutes min"
}
