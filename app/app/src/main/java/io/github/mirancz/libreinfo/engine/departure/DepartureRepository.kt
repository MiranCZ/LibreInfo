package io.github.mirancz.libreinfo.engine.departure

import android.content.Context
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureBoard
import io.github.mirancz.libreinfo.parsing.types.departure.DepartureHeader

interface DepartureRepository {

    fun headerOrNull(context: Context, stopId: Short): DepartureHeader?

    suspend fun header(context: Context, stopId: Short): DepartureHeader

    suspend fun board(context: Context, stopId: Short, maxEntries: Int, forceRefresh: Boolean): DepartureBoard
}