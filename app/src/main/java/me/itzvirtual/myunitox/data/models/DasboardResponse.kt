package me.itzvirtual.myunitox.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.data.remote.CinecaDateSerializer

// Used in Dashboard view

@Serializable
data class DashboardDataResponse(
    @SerialName("grades") val grades: List<Grade>,
    @SerialName("career") val career: Career,
    @SerialName("stats") val stats: StudentStats
)

@Serializable
data class Grade(
    @SerialName("Language") val language: String,
    @SerialName("IdStudent") val idStudent: Long,
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,

    @Serializable(with = CinecaDateSerializer::class)
    @SerialName("date") val date: String,

    @SerialName("value") val value: Double,
    @SerialName("average") val average: Double,
    @SerialName("position") val position: Int
)

@Serializable
data class Career(
    @SerialName("Language") val language: String,
    @SerialName("id") val id: Int? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("total") val total: Double,
    @SerialName("registered") val registered: Double,
    @SerialName("calculated") val calculated: Double,
    @SerialName("idStudent") val idStudent: Long
)

@Serializable
data class StudentStats(
    @SerialName("PersId") val personId: Int? = null,
    @SerialName("Matricola") val matriculationNumber: String? = null,
//    @SerialName("TipoMediaCod") val meanTypeCode: String,
//    @SerialName("AdPianif") val plannedActivities: Int,
//    @SerialName("CfuAdPianif") val plannedCredits: Int,
//    @SerialName("CfuPiano") val totalPlanCredits: Int,

    // wtf are these two
//    @SerialName("BaseVotoAritmetica") val arithmeticBaseVote: Double,
//    @SerialName("BaseVotoPonderata") val weightedBaseVote: Double,

//    @SerialName("MaxPuntiCds") val maxDegreePoints: Int,
//    @SerialName("MaxPuntiEsa") val maxExamPoints: Int,
    @SerialName("num_exams") val numExams: Int,

    @SerialName("arithmetic_avg") val arithmeticAvg: Double,
    @SerialName("weight_avg") val weightAvg: Double,

    @SerialName("cfu_gained") val creditsGained: Double,
    @SerialName("cfu_calculated") val creditsCalculated: Double,
    @SerialName("cfu_tot") val totalCredits: Int,
    @SerialName("cfu_progression") val progressionPercentage: Double,

    @SerialName("num_completed_activities") val completedActivities: Int,
    @SerialName("num_remaining_activities") val remainingActivities: Int,


    @SerialName("idStudent") val studentId: Long,
    @SerialName("medie") val averages: List<Mean>,

)

@Serializable
data class Mean(
    @SerialName("base") val base: Int,
    @SerialName("definizione") val definition: String,
    @SerialName("tipo") val type: String, // "A" or "P"
    @SerialName("value") val value: Double
)