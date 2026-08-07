package io.github.mirancz.libreinfo.parsing.types.dto

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.LineAlias
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.parsing.types.stop.StopId


fun mapStop(storage: IdStorage, stopId: Int): Stop =
    storage.stopStorage.getStop(StopId.original(stopId))

fun mapLine(storage: IdStorage, lineId: Int): LineAlias =
    storage.lineStorage.getAlias(lineId)