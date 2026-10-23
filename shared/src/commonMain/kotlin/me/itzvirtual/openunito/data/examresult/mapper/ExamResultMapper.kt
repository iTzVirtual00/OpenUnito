package me.itzvirtual.openunito.data.examresult.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.examresult.local.ExamResultEntity
import me.itzvirtual.openunito.data.examresult.remote.ExamResultDto
import me.itzvirtual.openunito.data.examresult.remote.GradeResultDto
import me.itzvirtual.openunito.domain.examresult.ExamResult
import me.itzvirtual.openunito.domain.examresult.GradeResult
import me.itzvirtual.openunito.domain.getNowMillis

fun ExamResultDto.toDomain() = ExamResult(
    id = id, courseName = courseName, courseCode = courseCode,
    date =
        date,
    weight = weight, editableUntil = editableUntil,
    maxGrade = maxGrade, status = status, gradeResult = gradeResult?.toDomain(),
)

fun GradeResultDto.toDomain() = GradeResult(
    passed = passed, grade = grade, isAbsent = isAbsent,
    isWithdrawn = isWithdrawn, canAccept = canAccept,
    canReject = canReject, laude = laude,
)

fun List<ExamResultDto>.toEntity(json: Json, careerId: Int) =
    ExamResultEntity(
        id = careerId,
        data = json.encodeToString(this),
        cachedAt = getNowMillis()
    )

fun ExamResultEntity.toDomain(json: Json): List<ExamResult> =
    json.decodeFromString<List<ExamResultDto>>(data).map { it.toDomain() }
