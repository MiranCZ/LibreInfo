package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.dto.DiversionDTO
import kotlinx.serialization.Serializable

@Serializable
data class DiversionsResponse(val diversions: List<DiversionDTO>)
