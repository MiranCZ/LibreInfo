package io.github.mirancz.libreinfo.parsing.types

import kotlinx.serialization.Serializable

@Serializable
data class StopDelayEntry(val delay: Int, val lastUpdated: Long)
