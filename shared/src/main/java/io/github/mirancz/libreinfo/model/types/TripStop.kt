package io.github.mirancz.libreinfo.model.types

import kotlinx.serialization.Serializable

/**
 * Single stop of a specific [TripSchedule][io.github.mirancz.libreinfo.model.TripSchedule]. Also, a single row of [GTFS stop times](https://gtfs.org/documentation/schedule/reference/#stop_timestxt)
 *
 * @see [io.github.mirancz.libreinfo.model.Trip]
 * @see [io.github.mirancz.libreinfo.model.TripSchedule]
 */
@Serializable
data class TripStop(
    val id: Int,
    val tripId: Int,
    val stopId: Short,
    val postId: Short,
    val sequence: Int,
    val arrival: Time,
    val departure: Time
)
