package me.itzvirtual.myunitox.data.examresult.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.core.network.GradeStatusSerializer
import me.itzvirtual.myunitox.core.network.IntToBooleanSerializer
import me.itzvirtual.myunitox.core.network.SmartNumberSerializer
import me.itzvirtual.myunitox.domain.examresult.GradeStatus

@Serializable
data class ExamResultDto(
    @SerialName("applistaId") val id: String = "",
    @SerialName("adStuDes") val courseName: String = "",
    @SerialName("adStuCod") val courseCode: String = "",
    @SerialName("dataEsa") val date: String = "",
    @SerialName("pesoAd") val weight: Int = 0,
    @SerialName("dataRifEsitoStu") val editableUntil: String = "",
    @SerialName("gruppoVotoMaxPunti") @Serializable(with = SmartNumberSerializer::class) val maxGrade: String = "",
    @SerialName("presaVisione") @Serializable(with = GradeStatusSerializer::class) val status: GradeStatus,
    @SerialName("esito") val gradeResult: GradeResultDto?,
)

@Serializable
data class GradeResultDto(
    @Serializable(with = IntToBooleanSerializer::class)
    @SerialName("superatoFlg") val passed: Boolean,

    @SerialName("votoEsa") @Serializable(with = SmartNumberSerializer::class) val grade: String = "",

    @Serializable(with = IntToBooleanSerializer::class)
    @SerialName("assenteFlg") val isAbsent: Boolean,

    @Serializable(with = IntToBooleanSerializer::class)
    @SerialName("ritiratoFlg") val isWithdrawn: Boolean,

    @Serializable(with = IntToBooleanSerializer::class)
    @SerialName("acceptEnabled") val canAccept: Boolean,

    @Serializable(with = IntToBooleanSerializer::class)
    @SerialName("rejectEnabled") val canReject: Boolean,

    @Serializable(with = IntToBooleanSerializer::class)
    @SerialName("laude") val laude: Boolean = false,
)
