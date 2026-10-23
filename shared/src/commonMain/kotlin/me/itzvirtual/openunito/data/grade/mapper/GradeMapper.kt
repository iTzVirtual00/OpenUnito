package me.itzvirtual.openunito.data.grade.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.grade.local.ExamGradeEntity
import me.itzvirtual.openunito.data.grade.remote.ExamGradeDto
import me.itzvirtual.openunito.data.grade.remote.ScoreDto
import me.itzvirtual.openunito.domain.getNowMillis
import me.itzvirtual.openunito.domain.grade.ExamGrade
import me.itzvirtual.openunito.domain.grade.Score

fun ExamGradeDto.toDomain() = ExamGrade(
    id = id, courseName = courseName, score = score?.toDomain(),
    teaching = teaching, status = status, passYear = passYear,
    yearFreq = yearFreq, ordAdCod = ordAdCod, ordAdDes = ordAdDes,
    yearIdx = yearIdx, weight = weight,
)

fun ScoreDto.toDomain() = Score(date = date, value = value, laude = laude)

fun List<ExamGradeDto>.toEntity(json: Json, careerId: Int) =
    ExamGradeEntity(id = careerId, data = json.encodeToString(this), cachedAt = getNowMillis())

fun ExamGradeEntity.toDomain(json: Json): List<ExamGrade> =
    json.decodeFromString<List<ExamGradeDto>>(data).map { it.toDomain() }
