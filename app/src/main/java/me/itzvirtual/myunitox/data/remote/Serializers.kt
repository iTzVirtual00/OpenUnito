package me.itzvirtual.myunitox.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

object CinecaDateSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("CinecaDate", PrimitiveKind.STRING)

    private val regex = Regex("""/Date\((\d+)([+-]\d+)?\)/""")
    private val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    override fun deserialize(decoder: Decoder): String {
        val input = decoder.decodeString()
        val match = regex.find(input)

        return if (match != null) {
            val milliseconds = match.groupValues[1].toLong()
            val date = Date(milliseconds)
            outputFormat.format(date)
        } else {
            input // Fallback to raw string if regex fails
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}