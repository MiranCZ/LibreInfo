package io.github.mirancz.libreinfo.parsing.types

import io.github.mirancz.libreinfo.model.Stop

data class PreviousStop(val stop: Stop, val delay: Int)