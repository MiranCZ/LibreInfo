package io.github.mirancz.libreinfo.engine.departure

import android.content.Context
import io.github.mirancz.libreinfo.engine.StorageProvider
import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureBoard
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureHeader
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.util.OfflineDepartures
import io.github.mirancz.libreinfo.util.load.toAppException
import io.github.mirancz.libreinfo.util.request.RequestHelper

class OfflineDepartureRepository(val storageProvider: StorageProvider) : DepartureRepository {

    override fun headerOrNull(context: Context, stopId: Int): DepartureHeader? {
        val storage = storageProvider.getOrNull() ?: return null

        return header(storage, stopId)
    }

    override suspend fun header(context: Context, stopId: Int): DepartureHeader {
        val storage = storageProvider.get()

        return header(storage, stopId)
    }

    private fun header(storage: IdStorage, stopId: Int): DepartureHeader {
        val stop = storage.stopStorage.getStop(StopId.internal(stopId))
        val stopName = stop.name

        return DepartureHeader(stopName, storage.postStorage.getPosts(stop).map { it.name })
    }

    override suspend fun board(context: Context, stopId: Int, maxEntries: Int, forceRefresh: Boolean): DepartureBoard {
        val storage = storageProvider.get()

        var error: AppException? = null
        var delays: RouteDelaysResponse? = null
        try {
            delays = RequestHelper.getRouteDelays(context, force = forceRefresh)
        } catch (e: RequestException) {
            error = e.toAppException()
        }

        val stop = storage.stopStorage.getStop(StopId.internal(stopId))

        return DepartureBoard(
            stop,
            null,
            error,
            OfflineDepartures.getOffline(
                storage,
                stopId,
                maxEntries,
                delays
            ).map { it.toPostDeparture(true) }
        )
    }


}