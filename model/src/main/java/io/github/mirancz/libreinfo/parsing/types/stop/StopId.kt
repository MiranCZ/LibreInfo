package io.github.mirancz.libreinfo.parsing.types.stop

import kotlinx.serialization.Serializable

@Serializable
data class StopId(val id: Int) {

    companion object {
        val NONE = StopId(-1)

    }

}