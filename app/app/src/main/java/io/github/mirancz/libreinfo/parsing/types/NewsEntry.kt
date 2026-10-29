package io.github.mirancz.libreinfo.parsing.types

import android.os.Parcelable
import androidx.core.text.HtmlCompat
import io.github.mirancz.libreinfo.parsing.types.serial.IsoDateTimeSerializer
import io.github.mirancz.libreinfo.parsing.types.serial.NullableDateTimeParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Serializable
data class NewsEntry(
    val title: String,
    val content: String,
    val published: @Serializable(IsoDateTimeSerializer::class) DateTime?,
    val url: String?
) {

    fun getPlaintext(): String {
        return HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    }
}
