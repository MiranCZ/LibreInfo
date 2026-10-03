package io.github.mirancz.libreinfo.parsing.types.stop

import io.github.mirancz.libreinfo.parsing.types.Location
import kotlinx.serialization.Serializable

@Serializable
data class Stop(
    val id: StopId,
    val name: String,
    val parentStation: String,
    val location: Location
) {

    companion object {
        val NONE: Stop = Stop(StopId.NONE, "UNKNOWN", "UNKNOWN", Location.NONE)
    }



}