package io.github.mirancz.libreinfo.parsing.types.dto

import io.github.mirancz.libreinfo.parsing.types.StopDelayEntry
import kotlinx.serialization.Serializable

@Serializable
data class StopDelaysResponse(val stopDelays: Map<Int, Map<Int, StopDelayEntry>>)
