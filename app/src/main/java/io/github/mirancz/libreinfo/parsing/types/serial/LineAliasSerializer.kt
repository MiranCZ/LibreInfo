package io.github.mirancz.libreinfo.parsing.types.serial

import io.github.mirancz.libreinfo.parsing.types.LineAlias
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LineAliasSerializer : KSerializer<LineAlias> {

    @Serializable
    @SerialName("io.github.mirancz.libreinfo.LineAlias")
    private class Surrogate(
        val id: Int,
        val lineDisplayName: String,
        val backgroundColor: Int,
        val backgroundColorStr: String,
        val textColor: Int,
        val textColorStr: String,
    )

    override val descriptor: SerialDescriptor = Surrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: LineAlias) {
        val surrogate = Surrogate(
            value.id, value.lineDisplayName,
            value.backgroundColor, value.backgroundColorStr,
            value.textColor, value.textColorStr,
        )
        encoder.encodeSerializableValue(Surrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): LineAlias {
        val s = decoder.decodeSerializableValue(Surrogate.serializer())
        return LineAlias(s.id, s.lineDisplayName, s.backgroundColor, s.backgroundColorStr, s.textColor, s.textColorStr)
    }
}
