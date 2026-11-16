package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.model.types.TripStop
import kotlinx.serialization.Serializable

/**
 * Concrete schedule of a [Trip] (with one-to-one mapping with it) i.e. the collection of stops the specific [Trip] mapped with [tripId] goes through.
 *
 * The two classes are not merged because [Trip] serves as a general `display` data (`headsign`, `lineId`, ...), but schedule serves as `compute` data (with plans to split these two data types in the future)
 *
 * @see Trip
 * @see TripStop
 */
@Serializable
data class TripSchedule(
    val tripId: Int,
    val startPos: Int,
    val length: Int,
    val stops: List<TripStop>
)
