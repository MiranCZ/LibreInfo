package io.github.mirancz.libreinfo.parsing.types.departure

import io.github.mirancz.libreinfo.parsing.types.TimeMark

sealed interface DepartureTime {
    data class Scheduled(val mark: TimeMark) : DepartureTime

    // the server's preformatted string, for now
    data class Verbatim(val text: String) : DepartureTime
}