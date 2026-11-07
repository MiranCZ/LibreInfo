package io.github.mirancz.libreinfo.model.types

import kotlinx.serialization.Serializable

@Serializable
data class RouteStop(
    val id: Int,
    val tripId: Int,
    val stopId: Short,
    val postId: Short,
    val sequence: Int,
    val arrival: Time,
    val departure: Time
)
