package io.github.mirancz.libreinfo.model.types


data class StopPattern(val stops: List<StopPost>) {
    val trips = ArrayList<Int>()
}
