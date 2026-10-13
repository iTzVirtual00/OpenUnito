package me.itzvirtual.myunitox.data.grade.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.grade.local.ExamGradeEntity
import me.itzvirtual.myunitox.data.grade.remote.ExamGradeDto
import me.itzvirtual.myunitox.data.grade.remote.ScoreDto
import me.itzvirtual.myunitox.domain.grade.ExamGrade
import me.itzvirtual.myunitox.domain.grade.Score

fun ExamGradeDto.toDomain() = ExamGrade(
    id = id, courseName = courseName, score = score?.toDomain(),
    teaching = teaching, status = status, passYear = passYear,
    yearFreq = yearFreq, ordAdCod = ordAdCod, ordAdDes = ordAdDes,
    yearIdx = yearIdx, weight = weight,
)

fun ScoreDto.toDomain() = Score(date = date, value = value, laude = laude)

fun List<ExamGradeDto>.toEntity(json: Json) =
    ExamGradeEntity(id = 0, data = json.encodeToString(this), cachedAt = System.currentTimeMillis())

fun ExamGradeEntity.toDomain(json: Json): List<ExamGrade> =
    json.decodeFromString<List<ExamGradeDto>>(data).map { it.toDomain() }
