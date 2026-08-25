package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage
import io.github.mirancz.libreinfo.parsing.types.VehicleInfo
import io.github.mirancz.libreinfo.parsing.types.dto.PreviousStopDTO
import io.github.mirancz.libreinfo.parsing.types.dto.mapStop
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import kotlinx.serialization.Serializable

@Serializable
data class VehicleInfoResponse(
    val vehicleId: Int,
    val delay: Int,
    val lastStopId: Int,
    val lastObservedAt: Long,
    val lastModifiedAt: Long,
    val previousStops: List<PreviousStopDTO>
) {

    fun map(storage: IdStorage): VehicleInfo =
        VehicleInfo(vehicleId, delay, mapStop(storage, lastStopId), lastObservedAt, lastModifiedAt,previousStops.map { it.map(storage) })


}
