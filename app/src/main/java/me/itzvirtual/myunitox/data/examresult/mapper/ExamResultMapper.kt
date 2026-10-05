package me.itzvirtual.myunitox.data.examresult.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.examresult.local.ExamResultEntity
import me.itzvirtual.myunitox.data.examresult.remote.ExamResultDto
import me.itzvirtual.myunitox.data.examresult.remote.GradeResultDto
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.examresult.GradeResult

fun ExamResultDto.toDomain() = ExamResult(
    id = id, courseName = courseName, courseCode = courseCode,
    date =
        date, weight = weight, editableUntil = editableUntil,
    maxGrade = maxGrade, status = status, gradeResult = gradeResult?.toDomain(),
)

fun GradeResultDto.toDomain() = GradeResult(
    passed = passed, grade = grade, isAbsent = isAbsent,
    isWithdrawn = isWithdrawn, canAccept = canAccept,
    canReject = canReject, laude = laude,
)

fun List<ExamResultDto>.toEntity(json: Json) =
    ExamResultEntity(
        id = 0,
        data = json.encodeToString(this),
        cachedAt = System.currentTimeMillis()
    )

fun ExamResultEntity.toDomain(json: Json): List<ExamResult> =
    json.decodeFromString<List<ExamResultDto>>(data).map { it.toDomain() }
