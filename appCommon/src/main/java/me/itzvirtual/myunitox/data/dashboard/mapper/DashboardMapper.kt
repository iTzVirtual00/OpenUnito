package me.itzvirtual.myunitox.data.dashboard.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.dashboard.local.DashboardEntity
import me.itzvirtual.myunitox.data.dashboard.remote.CareerDto
import me.itzvirtual.myunitox.data.dashboard.remote.DashboardDataResponseDto
import me.itzvirtual.myunitox.data.dashboard.remote.GradeDto
import me.itzvirtual.myunitox.data.dashboard.remote.MeanDto
import me.itzvirtual.myunitox.data.dashboard.remote.StudentStatsDto
import me.itzvirtual.myunitox.domain.dashboard.Career
import me.itzvirtual.myunitox.domain.dashboard.DashboardData
import me.itzvirtual.myunitox.domain.dashboard.Grade
import me.itzvirtual.myunitox.domain.dashboard.Mean
import me.itzvirtual.myunitox.domain.dashboard.StudentStats

fun DashboardDataResponseDto.toDomain() = DashboardData(
    grades = grades.map { it.toDomain() },
    career = career.toDomain(),
    stats = stats.toDomain(),
)

fun GradeDto.toDomain() = Grade(
    language = language, idStudent = idStudent, id = id,
    name = name, description = description, date = date,
    value = value, average = average, position = position,
)

fun CareerDto.toDomain() = Career(
    language = language, id = id, description = description,
    total = total, registered = registered, calculated = calculated, idStudent = idStudent,
)

fun StudentStatsDto.toDomain() = StudentStats(
    personId = personId, matriculationNumber = matriculationNumber,
    totalPlanCredits = totalPlanCredits, numExams = numExams,
    arithmeticAvg = arithmeticAvg, weightAvg = weightAvg,
    creditsGained = creditsGained, totalCredits = totalCredits,
    progressionPercentage = progressionPercentage,
    completedActivities = completedActivities, remainingActivities = remainingActivities,
    studentId = studentId, averages = averages.map { it.toDomain() },
)

fun MeanDto.toDomain() = Mean(base = base, definition = definition, type = type, value = value)

fun DashboardDataResponseDto.toEntity(json: Json) =
    DashboardEntity(id = 0, data = json.encodeToString(this), cachedAt = System.currentTimeMillis())

fun DashboardEntity.toDomain(json: Json): DashboardData =
    json.decodeFromString<DashboardDataResponseDto>(data).toDomain()
