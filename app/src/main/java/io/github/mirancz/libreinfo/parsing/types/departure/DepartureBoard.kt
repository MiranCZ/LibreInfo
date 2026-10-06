package io.github.mirancz.libreinfo.parsing.types.departure

import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.parsing.types.stop.Stop

data class DepartureBoard(
    val stop: Stop,
    val message: String?,
    val error: AppException?,
    val postDepartures: List<PostDeparture>
)
