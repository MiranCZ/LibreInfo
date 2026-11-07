package io.github.mirancz.libreinfo.model.types

import kotlinx.serialization.Serializable

@Serializable
data class Date(
    val day: Byte,
    val month: Byte,
    val year: Short
)
