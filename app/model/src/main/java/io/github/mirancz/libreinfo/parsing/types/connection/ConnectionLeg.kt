package io.github.mirancz.libreinfo.parsing.types.connection

import io.github.mirancz.libreinfo.parsing.types.DateTime
import io.github.mirancz.libreinfo.parsing.types.stop.Stop

data class ConnectionLeg(
    val departure: DateTime,
    val arrival: DateTime,
    val fromStop: Stop,
    val toStop: Stop,
    val duration: Int,
    val mode: String,
    val geometry: String,
    val delay: Int,
    val realtime: Boolean,
    val transportMode: TransportMode
)