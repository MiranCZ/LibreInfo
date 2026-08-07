package io.github.mirancz.libreinfo.parsing.types.response

import io.github.mirancz.libreinfo.parsing.types.dto.VehicleDTO
import kotlinx.serialization.Serializable

@Serializable
data class VehiclesResponse(val vehicles: List<VehicleDTO>)
