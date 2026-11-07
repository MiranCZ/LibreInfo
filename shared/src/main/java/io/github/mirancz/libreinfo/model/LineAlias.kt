package io.github.mirancz.libreinfo.model

import io.github.mirancz.libreinfo.codec.util.toCheckedShort
import kotlinx.serialization.Serializable

@Serializable
data class LineAlias(
    val id: Short,
    val displayName: String,
    val backgroundColor: Int,
    val textColor: Int
) {

    constructor(id: Int, displayName: String, backgroundColor: Int, textColor: Int) : this(
        id.toCheckedShort(), displayName, backgroundColor, textColor
    )

}