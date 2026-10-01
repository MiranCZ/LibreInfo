package io.github.mirancz.libreinfo.nav

import kotlinx.serialization.Serializable


sealed interface NavRoute {

    @Serializable
    data object Home : NavRoute

    @Serializable
    data object Search : NavRoute

    @Serializable
    data object VehicleMap : NavRoute

    @Serializable
    data object ConnectionSearch : NavRoute

    @Serializable
    data object VehiclesList : NavRoute

    @Serializable
    data object Events : NavRoute

    @Serializable
    data object Diversions : NavRoute

    @Serializable
    data object News : NavRoute

    @Serializable
    data object Settings : NavRoute {

        @Serializable
        data object Departures : NavRoute

        @Serializable
        data object Location : NavRoute

        @Serializable
        data object Updates : NavRoute

        @Serializable
        data object Dev : NavRoute
    }

    @Serializable
    data object About : NavRoute

}