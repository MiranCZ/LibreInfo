package io.github.mirancz.libreinfo.engine.departure

import android.content.Context
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureBoard
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureHeader

interface DepartureRepository {

    fun headerOrNull(context: Context, stopId: Int): DepartureHeader?

    suspend fun header(context: Context, stopId: Int): DepartureHeader

    suspend fun board(context: Context, stopId: Int, maxEntries: Int, forceRefresh: Boolean): DepartureBoard
}