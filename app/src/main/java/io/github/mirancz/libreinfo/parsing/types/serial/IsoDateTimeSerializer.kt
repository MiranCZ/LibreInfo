package io.github.mirancz.libreinfo.parsing.types.serial

import io.github.mirancz.libreinfo.parsing.types.DateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DateTimeException
import java.time.ZoneId

/**
 * Serializes [DateTime] as an ISO-8601 instant (eq. {@code 2026-07-26T12:30:00Z}), converted to
 * Prague wall-clock time on the way in and back to UTC on the way out.
 *
 * [DateTime.NONE] round-trips as {@code "?"}, the sentinel [DateTime.parse] already accepts.
 */
object IsoDateTimeSerializer : KSerializer<DateTime> {

    private val ZONE: ZoneId = ZoneId.of("Europe/Prague")
    private const val NONE_MARKER = "?"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.github.mirancz.libreinfo.DateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): DateTime {
        val raw = decoder.decodeString().trim()

        // the API is inconsistent about how it signals "no time"
        if (raw.isEmpty() || raw == NONE_MARKER) return DateTime.NONE

        return try {
            DateTime.parseISO8601(raw)
        } catch (e: DateTimeException) {
            throw SerializationException("Expected an ISO-8601 instant, got \"$raw\"", e)
        }
    }

    override fun serialize(encoder: Encoder, value: DateTime) {
        if (value == DateTime.NONE) {
            encoder.encodeString(NONE_MARKER)
            return
        }

        encoder.encodeString(value.toLocalDateTime().atZone(ZONE).toInstant().toString())
    }
}
