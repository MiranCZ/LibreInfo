package io.github.mirancz.libreinfo.parsing.types.response

import kotlinx.serialization.Serializable

@Serializable
data class DataInfoResponse(val lastUpdated: Long, val byteSize: Long)
