package io.github.mirancz.libreinfo.parsing.types.connection

import io.github.mirancz.libreinfo.parsing.types.DateTime


data class Connection(
    val departure: DateTime,
    val arrival: DateTime,
    val duration: Int,
    val transfers: Int,
    val legs: List<ConnectionLeg>
)
