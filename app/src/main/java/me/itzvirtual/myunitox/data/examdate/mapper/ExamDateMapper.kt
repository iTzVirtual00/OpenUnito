package me.itzvirtual.myunitox.data.examdate.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.examdate.local.ExamDateEntity
import me.itzvirtual.myunitox.data.examdate.remote.ExamDateDto
import me.itzvirtual.myunitox.domain.examdate.ExamDate

fun ExamDateDto.toDomain() = ExamDate(
    title = title, description = description, note = note,
    adCode = adCode, adDescription = adDescription, appDescription = appDescription,
    date = date, bookingStartDate = bookingStartDate, bookingEndDate = bookingEndDate,
    bookableExam = bookableExam, bookingEnabled = bookingEnabled,
    unbookingEnabled = unbookingEnabled, students = students, president = president,
)

fun List<ExamDateDto>.toEntity(json: Json) =
    ExamDateEntity(id = 0, data = json.encodeToString(this), cachedAt = System.currentTimeMillis())

fun ExamDateEntity.toDomain(json: Json): List<ExamDate> =
    json.decodeFromString<List<ExamDateDto>>(data).map { it.toDomain() }
