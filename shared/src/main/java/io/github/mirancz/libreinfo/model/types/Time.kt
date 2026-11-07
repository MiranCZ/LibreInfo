package io.github.mirancz.libreinfo.model.types

import kotlinx.serialization.Serializable

@Serializable
data class Time(
    val hours: UByte,
    val minutes: UByte
) {

    fun toMinutes(): Int {
        return (hours * 60u + minutes).toInt()
    }

}
