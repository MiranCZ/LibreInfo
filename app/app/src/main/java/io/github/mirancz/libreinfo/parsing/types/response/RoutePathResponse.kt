package io.github.mirancz.libreinfo.parsing.types.response

import kotlinx.serialization.Serializable

@Serializable
data class RoutePathResponse(val points: List<List<Double>>){

}

//@Serializable
//data class RoutePart(val stopId: Int, val path: String)
