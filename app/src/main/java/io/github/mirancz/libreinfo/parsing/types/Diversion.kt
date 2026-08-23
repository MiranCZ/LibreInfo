package io.github.mirancz.libreinfo.parsing.types

import android.os.Parcelable
import com.google.gson.JsonObject
import io.github.mirancz.libreinfo.parsing.storage.LineStorage
import kotlinx.parcelize.Parcelize

/**
 * This class is not [Serializable], see [io.github.mirancz.libreinfo.parsing.types.dto.DiversionDTO] instead
 */
@Parcelize
data class Diversion(
    val id: Int?,
    val title: String,
    val content: String,
    val from: DateTime,
    val to: DateTime,
    val lines: List<LineAlias>?
) : Parcelable {

}


