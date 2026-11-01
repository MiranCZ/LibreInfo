package io.github.mirancz.request.ids

data class RoutePathResponse(val serviceId: Int, val routeId: Int, val stops: List<RoutePart>){

}

data class RoutePart(val stopId: Int, val path: String)
