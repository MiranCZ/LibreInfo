package io.github.mirancz.libreinfo.parsing.types

import kotlinx.serialization.Serializable

@Serializable
data class RouteDelayEntry(val vehicleId: Int, val delay: Int, val lastUpdated: Long)
