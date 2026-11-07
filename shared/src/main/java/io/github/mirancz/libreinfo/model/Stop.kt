package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.model.types.Location
import io.github.mirancz.libreinfo.model.types.ParentStopStation
import kotlinx.serialization.Serializable

@Serializable
data class Stop(
    val id: Short,
    val name: String,
    val parentStation: ParentStopStation,
    val location: Location
) {

    companion object {
        val NONE: Stop = Stop(-1, "UNKNOWN", ParentStopStation.NONE, Location.NONE)
    }



}