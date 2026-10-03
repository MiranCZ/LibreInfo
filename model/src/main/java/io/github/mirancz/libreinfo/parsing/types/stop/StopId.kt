package io.github.mirancz.libreinfo.parsing.types.stop

import kotlinx.serialization.Serializable

@Serializable
data class StopId(val internal: Int, val original: Int) {

    companion object {
        val NONE = StopId(-1, -1)

        @JvmStatic
        fun internal(id: Int) =
            StopIdHolder(id, StopIdType.INTERNAL)

        @JvmStatic
        fun original(id: Int) =
            StopIdHolder(id, StopIdType.ORIGINAL)

    }

    @ConsistentCopyVisibility
    @Serializable
    data class StopIdHolder internal constructor(val id: Int, val type: StopIdType)

    enum class StopIdType {
        INTERNAL, ORIGINAL
    }
}