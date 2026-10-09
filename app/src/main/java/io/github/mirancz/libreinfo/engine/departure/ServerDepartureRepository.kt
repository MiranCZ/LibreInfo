package io.github.mirancz.libreinfo.engine.departure

import android.content.Context
import io.github.mirancz.libreinfo.exception.AppError
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.storage.manager.StorageProvider
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureBoard
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureEntry
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureHeader
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureTime
import io.github.mirancz.libreinfo.parsing.types.departure.PostDeparture
import io.github.mirancz.libreinfo.parsing.types.departure.VehicleInfo
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.parsing.types.stop.isFavourite
import io.github.mirancz.libreinfo.util.request.RequestHelper

class ServerDepartureRepository(val storageProvider: StorageProvider) : DepartureRepository {
    override fun headerOrNull(context: Context, stopId: Int): DepartureHeader? {
        val storage = storageProvider.getInstanceOrNull() ?: return null

        return header(storage, stopId)
    }

    override suspend fun header(context: Context, stopId: Int): DepartureHeader {
        val storage = storageProvider.getInstance()

        return header(storage, stopId)
    }

    private fun header(storage: IdStorage, stopId: Int): DepartureHeader {
        val stop = storage.stopStorage.getStop(stopId)
        val stopName = stop.name

        return DepartureHeader(stopName,stop.isFavourite(), storage.postStorage.getPosts(stop).map { it.name })
    }

    override suspend fun board(
        context: Context,
        stopId: Int,
        maxEntries: Int,
        forceRefresh: Boolean
    ): DepartureBoard {
        val storage = storageProvider.getInstance()

        val stop = storage.stopStorage.getStop(stopId)
        val response = RequestHelper.getDepartures(context, StopId(stopId))

        var error: AppError? = null

        var delays: RouteDelaysResponse? = null
        try {
            delays = RequestHelper.getRouteDelays(context, force = forceRefresh)
        } catch (e: RequestException) {
            error = e.error
        }


        return DepartureBoard(
            stop,
            response.message,
            error,
            response.posts.map { p ->
                PostDeparture(p.postId, p.name,false, p.departures.map {
                    val info = VehicleInfo()

                    info.setDelay(delays?.routeDelays[it.lineId]?.get(it.routeId)?.delay)

                    DepartureEntry(
                        storage.lineStorage.getAlias(it.lineId),
                        it.finalStop,
                        stopId,
                        p.postId,
                        it.isLowFloor,
                        DepartureTime.Verbatim(it.time),
                        storage.apiStorage.getTripId(it.lineId, it.routeId),
                        info
                    )
                })
            }
        )
    }
}