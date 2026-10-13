package me.itzvirtual.myunitox.domain.dashboard

import java.util.Date

data class DashboardData(
    val grades: List<Grade>,
    val career: Career,
    val stats: StudentStats,
)

data class Grade(
    val language: String,
    val idStudent: Long,
    val id: Int?,
    val name: String,
    val description: String,
    val date: Date,
    val value: String,
    val average: String,
    val position: Int,
)

data class Career(
    val language: String,
    val id: Int?,
    val description: String?,
    val total: String,
    val registered: String,
    val calculated: String,
    val idStudent: Long,
)

data class StudentStats(
    val personId: Int?,
    val matriculationNumber: String?,
    val totalPlanCredits: Int,
    val numExams: Int,
    val arithmeticAvg: Double,
    val weightAvg: Double,
    val creditsGained: String,
    val totalCredits: Int,
    val progressionPercentage: Double,
    val completedActivities: Int,
    val remainingActivities: Int,
    val studentId: Long,
    val averages: List<Mean>,
)

data class Mean(
    val base: Int,
    val definition: String,
    val type: String,
    val value: Double,
)

val dashboardDataSample = DashboardData(
    grades = listOf(
        Grade(
            language = "en",
            idStudent = 1483920,
            id = null,
            name = "Programmazione I",
            description = "30",
            date = Date(),
            value = "30",
            average = "30",
            position = 1
        ),
        Grade(
            language = "en",
            idStudent = 1483920,
            id = null,
            name = "Matematica Discreta",
            description = "25",
            date = Date(),
            value = "25",
            average = "27",
            position = 2
        ),
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
        matriculationNumber = "12345",
        totalPlanCredits = 180,
        numExams = 5,
        arithmeticAvg = 27.5,
        weightAvg = 27.2,
        creditsGained = "24",
        totalCredits = 180,
        progressionPercentage = 13.3,
        completedActivities = 5,
        remainingActivities = 25,
        studentId = 1483920,
        averages = listOf(Mean(base = 30, definition = "Aritmetica", type = "A", value = 27.5))
    ),
)
