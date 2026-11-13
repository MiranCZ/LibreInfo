package io.github.mirancz.libreinfo.parsing.types.dto

import io.github.mirancz.libreinfo.model.LineAlias
import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.model.Stop


fun mapStop(storage: IdStorage, stopId: Int): Stop =
    storage.stopStorage.getStop(stopId)

fun mapLine(storage: IdStorage, lineId: Int): LineAlias =
    storage.lineStorage.getAlias(lineId)