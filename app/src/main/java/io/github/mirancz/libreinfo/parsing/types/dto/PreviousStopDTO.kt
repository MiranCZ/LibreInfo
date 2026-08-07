package io.github.mirancz.libreinfo.parsing.types.dto

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.PreviousStop
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import kotlinx.serialization.Serializable

@Serializable
data class PreviousStopDTO(val id: Int, val delay: Int) {

    fun map(storage: IdStorage): PreviousStop =
        PreviousStop(mapStop(storage, id), delay)


}
