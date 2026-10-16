package me.itzvirtual.openunito.data.grade.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.openunito.core.network.CFUStringSerializer
import me.itzvirtual.openunito.core.network.MicrosoftDateSerializer
import me.itzvirtual.openunito.core.network.SmartNumberSerializer
import kotlin.time.Instant

@Serializable
data class ExamGradeDto(
    @SerialName("id") val id: String,
    @SerialName("description") val courseName: String = "?",
    @SerialName("score") val score: ScoreDto? = null,
    @SerialName("TeachingDetail") val teaching: String = "",
    @SerialName("StatusDetail") val status: String = "",  // S: superata, F: frequentata, P: pianificata
    @SerialName("PassYearDetail") val passYear: Int = 0,
    @SerialName("AnnoFreq") val yearFreq: String = "",
    @SerialName("OrdAdCod") val ordAdCod: String = "",
    @SerialName("OrdAdDes") val ordAdDes: String = "",
    @SerialName("year") val yearIdx: Int = 0,

    @SerialName("weight")
    @Serializable(with = CFUStringSerializer::class)
    val weight: String,
)

@Serializable
data class ScoreDto(
    @SerialName("date")
    @Serializable(with = MicrosoftDateSerializer::class)
    val date: Instant,

    @SerialName("value")
    @Serializable(with = SmartNumberSerializer::class)
    val value: String,

    @SerialName("laude") var laude: Boolean,
)
