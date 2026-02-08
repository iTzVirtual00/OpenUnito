package me.itzvirtual.myunitox.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamResult(
    @SerialName("adStuDes")
    val courseName: String = "",

    @SerialName("dataEsa")
    val date: String = "",

    @SerialName("pesoAd")
    val weight: Int = 0,

    @SerialName("dataRifEsitoStu")
    val editableUntil: String = "",

    @SerialName("gruppoVotoCod")
    val grade: String = "",

    @SerialName("gruppoVotoMaxPunti")
    val maxGrade: Int = -1
)

