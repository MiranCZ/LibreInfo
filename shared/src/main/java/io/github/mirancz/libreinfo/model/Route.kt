package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.model.types.RouteStop
import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val tripId: Int,
    val startPos: Int,
    val length: Int,
    val stops: List<RouteStop>
)
