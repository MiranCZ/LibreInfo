package io.github.mirancz.libreinfo.parsing.types.departure

import io.github.mirancz.libreinfo.exception.AppError
import io.github.mirancz.libreinfo.parsing.types.stop.Stop

data class DepartureBoard(
    val stop: Stop,
    val message: String?,
    val error: AppError?,
    val postDepartures: List<PostDeparture>
)
