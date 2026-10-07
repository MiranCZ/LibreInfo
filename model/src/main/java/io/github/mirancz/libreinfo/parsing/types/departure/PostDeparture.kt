package io.github.mirancz.libreinfo.parsing.types.departure

data class PostDeparture(
    val postId: Int,
    val name: String,
    val detailAvailable: Boolean,
    val entries: List<DepartureEntry>
)
