package me.itzvirtual.openunito.data.student.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.student.local.StudentEntity
import me.itzvirtual.openunito.data.student.remote.AuthSessionDto
import me.itzvirtual.openunito.data.student.remote.StudentCareerDto
import me.itzvirtual.openunito.domain.getNowMillis
import me.itzvirtual.openunito.domain.student.StudentCareer
import me.itzvirtual.openunito.domain.student.StudentData

fun AuthSessionDto.toDomain() = StudentData(
    userId = userId, username = username,
    firstname = firstname, lastname = lastname,
    careers = careers.map { it.toDomain() }, type = type,
)

fun StudentCareerDto.toDomain() = StudentCareer(
    id = id, title = title, description = description,
    dateStart = dateStart, dateEnd = dateEnd,
    studentId = studentId, registrationId = registrationId,
    notes = notes, regulationYear = regulationYear,
    normativa = normativa, className = className,
    cohort = cohort, percorsoDes = percorsoDes,
    sedeDes = sedeDes, iscrDes = iscrDes,
    staStuDes = staStuDes, courseYear = courseYear,
)

fun AuthSessionDto.toEntity(json: Json) =
    StudentEntity(id = 0, data = json.encodeToString(this), cachedAt = getNowMillis())

fun StudentEntity.toDomain(json: Json): StudentData =
    json.decodeFromString<AuthSessionDto>(data).toDomain()
