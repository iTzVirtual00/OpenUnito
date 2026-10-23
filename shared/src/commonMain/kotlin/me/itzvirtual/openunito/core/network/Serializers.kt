package me.itzvirtual.openunito.core.network

import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import kotlin.time.Instant

object MicrosoftDateSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("CinecaDate", PrimitiveKind.STRING)

    private val regex = Regex("""/Date\((\d+)([+-]\d+)?\)/""")
    private val fixedOffset = UtcOffset(hours = 1) // TODO: think about this

    override fun deserialize(decoder: Decoder): Instant = deserializeString(decoder.decodeString())

    fun deserializeString(input: String): Instant {
        val match = regex.find(input)
        return if (match != null) {
            val millis = match.groupValues[1].toLong()
            Instant.fromEpochMilliseconds(millis)
        } else {
            Instant.fromEpochMilliseconds(0L)
        }
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        val offsetString = fixedOffset.format(UtcOffset.Formats.FOUR_DIGITS)
        encoder.encodeString("/Date(${value.toEpochMilliseconds()}$offsetString)/")
    }
}

object SmartNumberSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SmartNumberSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        // We cast to JsonDecoder to access the raw JSON element
        val input = (decoder as JsonDecoder).decodeJsonElement() as? JsonPrimitive ?: return ""
        // show decimal only if needed
        return when {
            input.isString -> input.content
            else -> {
                val doubleValue = input.doubleOrNull ?: 0.0
                if (doubleValue % 1.0 == 0.0) doubleValue.toInt().toString()
                else doubleValue.toString()
            }
        }
    }

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}

object CFUStringSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("CFUStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val input = (decoder as JsonDecoder).decodeJsonElement() as? JsonPrimitive ?: return "??"
        if (input.isString) {
            val stringValue = input.content.split(" ", limit = 2).getOrNull(0) ?: ""
            if (stringValue.toIntOrNull() != null) return stringValue
        }
        return "??"
    }

    override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}

object GradeStatusSerializer : KSerializer<GradeStatus> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("GradeStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): GradeStatus {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("GradeStatusSerializer expects a JSON")
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        val stringValue = jsonObject["value"]?.jsonPrimitive?.content ?: return GradeStatus.UNKNOWN
        return fromSerialName(stringValue)
    }

    override fun serialize(encoder: Encoder, value: GradeStatus) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("GradeStatusSerializer expects JSON")

        val jsonObject = buildJsonObject {
            // Option A: Use the put(key, string) extension
            put("value", JsonPrimitive(serializeToString(value)))
        }

        jsonEncoder.encodeJsonElement(jsonObject)
    }

    fun serializeToString(value: GradeStatus): String = when (value) {
        GradeStatus.VIEWED -> "V"
        GradeStatus.ACCEPTED -> "A"
        GradeStatus.REJECTED -> "R"
        GradeStatus.NOT_TOUCHED -> "N"
        GradeStatus.UNKNOWN -> throw SerializationException("Cannot serialize UNKNOWN GradeStatus")
    }

    private fun fromSerialName(name: String): GradeStatus = when (name) {
        "V" -> GradeStatus.VIEWED
        "A" -> GradeStatus.ACCEPTED
        "R" -> GradeStatus.REJECTED
        "N" -> GradeStatus.NOT_TOUCHED
        else -> GradeStatus.UNKNOWN
    }
}

object IntToBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntToBooleanSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() == 1
    override fun serialize(encoder: Encoder, value: Boolean) =
        encoder.encodeInt(if (value) 1 else 0)
}

object InstantIsoSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeString(value.toString())
}
