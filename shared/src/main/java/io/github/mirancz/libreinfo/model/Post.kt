package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import io.github.mirancz.libreinfo.model.types.Location
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val stopId: Short,
    val postId: Short,
    val name: String,
    val location: Location
) {

    constructor(stopId: Int, postId: Int, name: String, location: Location) : this(
        stopId.toCheckedShort(), postId.toCheckedShort(), name, location
    )

}
