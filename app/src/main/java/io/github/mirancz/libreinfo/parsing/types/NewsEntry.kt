package io.github.mirancz.libreinfo.parsing.types

import android.os.Parcelable
import androidx.core.text.HtmlCompat
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class NewsEntry(
    val title: String,
    val content: String,
    val published: @Serializable(IsoDateTimeSerializer::class) DateTime?,
    val url: String?
) : Parcelable {

    fun getPlaintext(): String {
        return HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }
}
