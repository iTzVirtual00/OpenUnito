package me.itzvirtual.myunitox.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.itzvirtual.myunitox.data.models.GradeStatus
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

object MicrosoftDateSerializer : KSerializer<Date> {
    override val descriptor = PrimitiveSerialDescriptor("CinecaDate", PrimitiveKind.STRING)

    private val regex = Regex("""/Date\((\d+)([+-]\d+)?\)/""")
    private val offsetFormat = SimpleDateFormat("Z", Locale.US)
//    private val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    override fun deserialize(decoder: Decoder): Date {
        val input = decoder.decodeString()
        return deserializeString(input)
    }

    fun deserializeString(input: String): Date {
        val match = regex.find(input)

        return if (match != null) {
            val milliseconds = match.groupValues[1].toLong()
            val date = Date(milliseconds)
            date
        } else {
            Date(0) // Return epoch if the format is unexpected
        }
    }


    override fun serialize(encoder: Encoder, value: Date) {
        val ms = value.time
        val offset = offsetFormat.format(value)
        // Reconstructs: /Date(1707920400000+0100)/
        encoder.encodeString("/Date($ms$offset)/")
    }
}

object SmartNumberSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SmartNumberSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        // We cast to JsonDecoder to access the raw JSON element
        val input = (decoder as JsonDecoder).decodeJsonElement() as? JsonPrimitive
            ?: return ""

        return when {
            input.isString -> input.content

            // Handle Number logic
            else -> {
                val doubleValue = input.doubleOrNull ?: 0.0
                // Check if the decimal part is zero
                if (doubleValue % 1.0 == 0.0) {
                    doubleValue.toInt().toString()
                } else {
                    doubleValue.toString()
                }
            }
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}


object CFUStringSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("CFUStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val input = (decoder as JsonDecoder).decodeJsonElement() as? JsonPrimitive
            ?: return "??"
        when {
            input.isString -> {
                val stringValue = input.content.split(" ", limit = 2).getOrNull(0) ?: ""
                if (stringValue.toIntOrNull() != null) {
                    return stringValue
                }
            }
        }
        return "??"
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
//var tested = 0
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
        encoder.encodeString(serializeToString(value))
    }

    fun serializeToString(value: GradeStatus): String {
        return when (value) {
            GradeStatus.VIEWED -> "V"
            GradeStatus.ACCEPTED -> "A"
            GradeStatus.REJECTED -> "R"
            GradeStatus.NOT_TOUCHED -> "N"
            GradeStatus.UNKNOWN ->throw SerializationException("Cannot serialize UNKNOWN GradeStatus")
        }
    }

    private fun fromSerialName(name: String): GradeStatus {
        return when (name) {
            "V" -> GradeStatus.VIEWED
            "A" -> GradeStatus.ACCEPTED
            "R" -> GradeStatus.REJECTED
            "N" -> GradeStatus.NOT_TOUCHED
            else -> GradeStatus.UNKNOWN
        }
    }
}

object IntToBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntToBooleanSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Boolean {
        // Reads the Int from API and converts to Boolean
        return decoder.decodeInt() == 1
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        // Converts Boolean back to Int for the API
        encoder.encodeInt(if (value) 1 else 0)
    }
}

object InstantIsoSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString()) // Instant.toString() defaults to ISO-8601
    }
}