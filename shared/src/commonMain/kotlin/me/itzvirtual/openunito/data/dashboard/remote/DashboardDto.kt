package me.itzvirtual.openunito.data.dashboard.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.openunito.core.network.MicrosoftDateSerializer
import me.itzvirtual.openunito.core.network.SmartNumberSerializer
import kotlin.time.Instant

@Serializable
data class DashboardDataResponseDto(
    @SerialName("grades") val grades: List<GradeDto>,
    @SerialName("career") val career: CareerDto,
    @SerialName("stats") val stats: StudentStatsDto,
)

@Serializable
data class GradeDto(
    @SerialName("Language") val language: String,
    @SerialName("IdStudent") val idStudent: Long,
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("date") val date: Instant,

    @SerialName("value") @Serializable(with = SmartNumberSerializer::class) val value: String,
    @SerialName("average") @Serializable(with = SmartNumberSerializer::class) val average: String,
    @SerialName("position") val position: Int,
)

@Serializable
data class CareerDto(
    @SerialName("Language") val language: String,
    @SerialName("id") val id: Int? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("total") @Serializable(with = SmartNumberSerializer::class) val total: String,
    @SerialName("registered") @Serializable(with = SmartNumberSerializer::class) val registered: String,
    @SerialName("calculated") @Serializable(with = SmartNumberSerializer::class) val calculated: String,
    @SerialName("idStudent") val idStudent: Long,
)

@Serializable
data class StudentStatsDto(
    @SerialName("PersId") val personId: Int? = null,
    @SerialName("Matricola") val matriculationNumber: String? = null,
//    @SerialName("TipoMediaCod") val meanTypeCode: String,
//    @SerialName("AdPianif") val plannedActivities: Int,
//    @SerialName("CfuAdPianif") val plannedCredits: Int,
    @SerialName("CfuPiano") val totalPlanCredits: Int,

    // wtf are these two
//    @SerialName("BaseVotoAritmetica") val arithmeticBaseVote: Double,
//    @SerialName("BaseVotoPonderata") val weightedBaseVote: Double,

//    @SerialName("MaxPuntiCds") val maxDegreePoints: Int,
//    @SerialName("MaxPuntiEsa") val maxExamPoints: Int,
    @SerialName("num_exams") val numExams: Int,
    @SerialName("arithmetic_avg") val arithmeticAvg: Double,
    @SerialName("weight_avg") val weightAvg: Double,
    @SerialName("cfu_gained") @Serializable(with = SmartNumberSerializer::class) val creditsGained: String,
    @SerialName("cfu_tot") val totalCredits: Int,
    @SerialName("cfu_progression") val progressionPercentage: Double,
    @SerialName("num_completed_activities") val completedActivities: Int,
    @SerialName("num_remaining_activities") val remainingActivities: Int,
    @SerialName("idStudent") val studentId: Long,
    @SerialName("medie") val averages: List<MeanDto>,
)

@Serializable
data class MeanDto(
    @SerialName("base") val base: Int,
    @SerialName("definizione") val definition: String,
    @SerialName("tipo") val type: String,
    @SerialName("value") val value: Double,
)
