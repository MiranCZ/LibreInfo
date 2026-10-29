package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.RouteDelayEntry
import kotlinx.serialization.Serializable

@Serializable
data class RouteDelaysResponse(val routeDelays: Map<Int, Map<Int, RouteDelayEntry>>)
