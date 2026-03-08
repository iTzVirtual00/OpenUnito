package me.itzvirtual.myunitox.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.itzvirtual.myunitox.data.remote.MicrosoftDateSerializer
import me.itzvirtual.myunitox.data.remote.SmartNumberSerializer
import java.util.Date

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

    @Serializable(with = MicrosoftDateSerializer::class)
    @SerialName("date") val date: Date,


    @SerialName("value") @Serializable(with=SmartNumberSerializer::class) val value: String,
    @SerialName("average") @Serializable(with=SmartNumberSerializer::class) val average: String,
    @SerialName("position") val position: Int
)

@Serializable
data class Career(
    @SerialName("Language") val language: String,
    @SerialName("id") val id: Int? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("total") @Serializable(with=SmartNumberSerializer::class) val total: String,
    @SerialName("registered") @Serializable(with=SmartNumberSerializer::class) val registered: String,
    @SerialName("calculated") @Serializable(with=SmartNumberSerializer::class) val calculated: String,
    @SerialName("idStudent") val idStudent: Long
)

@Serializable
data class StudentStats(
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

    @SerialName("cfu_gained") @Serializable(with=SmartNumberSerializer::class)  val creditsGained: String,
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

val dashboardDataSample = DashboardDataResponse(
    grades = listOf(
        Grade(
            language = "en",
            idStudent = 1483920,
            id = null,
            name = "other activities",
            description = "29.000000",
            date = MicrosoftDateSerializer.deserializeString("/Date(1749420000000+0200)/"),
            value = "29",
            average = "29",
            position = 1
        ),
        Grade(
            language = "en",
            idStudent = 1483920,
            id = null,
            name = "OTHER ACTIVITIES",
            description = "18.000000",
            date = MicrosoftDateSerializer.deserializeString("/Date(1749420000000+0200)/"),
            value = "18",
            average = "23.5",
            position = 2
        ),
        Grade(
            language = "en",
            idStudent = 1483920,
            id = null,
            name = "DISCRETE MATHEMATICS, ALGEBRA AND GEOMETRY",
            description = "25.000000",
            date = MicrosoftDateSerializer.deserializeString("/Date(1768518000000+0100)/"),
            value = "25",
            average = "24",
            position = 3
        )
    ),
    career = Career(
        language = "eng",
        id = null,
        description = null,
        total = "180.0",
        registered = "24",
        calculated = "24",
        idStudent = 1483920
    ),
    stats = StudentStats(
        personId = null,
        matriculationNumber = null,
        numExams = 4,
        arithmeticAvg = 24.25,
        weightAvg = 24.25,
        creditsGained = "24",
        totalCredits = 180,
        progressionPercentage = 13.333333,
        completedActivities = 4,
        remainingActivities = 8,
        studentId = 1483920,
        averages = listOf(
            Mean(base = 30, definition = "CDSORD", type = "A", value = 24.0),
            Mean(base = 30, definition = "CDSORD", type = "P", value = 24.25)
        ),
        totalPlanCredits = 66
    )
)
