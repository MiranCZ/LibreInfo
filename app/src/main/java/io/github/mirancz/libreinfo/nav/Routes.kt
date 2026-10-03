package io.github.mirancz.libreinfo.nav

import io.github.mirancz.libreinfo.activity.SearchKinds
import io.github.mirancz.libreinfo.parsing.types.Diversion
import io.github.mirancz.libreinfo.parsing.types.NewsEntry
import kotlinx.serialization.Serializable


sealed interface NavRoute {

    @Serializable
    data object Home : NavRoute

    /**
     * Used to go into [io.github.mirancz.libreinfo.activity.DeparturesActivity] only, this is hardcoded in routing
     *
     * an **enter** animation is played when a stop is selected
     */
    @Serializable
    data class StopSearch(val prefetchDelays: Boolean = false) : NavRoute

    @Serializable
    data class Departures(val stopId: Int)

    /**
     * Can be used by any screen to pick a stop (, location or poi) and return back
     *
     * an **exit** animation is played when a stop is selected
     */
    @Serializable
    data class StopPicker(val resultKey: String, val kinds: SearchKinds) : NavRoute

    @Serializable
    data object VehicleMap : NavRoute

    @Serializable
    data object ConnectionSearch : NavRoute

    @Serializable
    data object VehiclesList : NavRoute

    @Serializable
    data object Events : NavRoute

    @Serializable
    data object Diversions : NavRoute {

        @Serializable
        data class Detail(val diversion: Diversion) : NavRoute

    }

    @Serializable
    data object News : NavRoute {
        @Serializable
        data class Detail(val entry: NewsEntry) : NavRoute
    }

    @Serializable
    data object Settings : NavRoute {

        @Serializable
        data object Departures : NavRoute

        @Serializable
        data object Location : NavRoute

        @Serializable
        data object Updates : NavRoute

        @Serializable
        data object Dev : NavRoute {

            @Serializable
            data object LineList : NavRoute

            @Serializable
            data object DeparturePerformance : NavRoute

        }
    }

    @Serializable
    data object About : NavRoute {

        @Serializable
        data object Attribution : NavRoute
    }

}