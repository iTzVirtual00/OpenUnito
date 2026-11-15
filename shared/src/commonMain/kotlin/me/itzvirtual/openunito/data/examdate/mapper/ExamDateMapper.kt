package me.itzvirtual.openunito.data.examdate.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.examdate.local.ExamDateEntity
import me.itzvirtual.openunito.data.examdate.remote.ExamDateDto
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.getNowMillis

/*
My understanding of the holy cryptic Cineca logic:

booked & modifiable:
"bookingEnabled": true,

"bookableExam": false,
"unbookingEnabled": true,

booked & not modifiable:
???

not booked & modifiable
"bookingEnabled": true,

"bookableExam": true,
"unbookingEnabled": false,

not booked & unmodifiable
"bookingEnabled": false,

"bookableExam": true,
"unbookingEnabled": false,
 */

fun ExamDateDto.toDomain() = ExamDate(
    isBooked = !bookableExam,
    canEdit = bookingEnabled,

    appId = appId,
    adsceId = adsceId,
    adId = adId,
    activityCode = cod,
    title = title,
    description = description,
    note = note,

    date = date,
    bookingStartDate = bookingStartDate,
    bookingEndDate = bookingEndDate,

    cdsAppId = cdsAppId,
    adAppId = adAppId,

    subscribedStudents = students,

    president = president,

    questionnaire = statoQuest
)

fun List<ExamDateDto>.toEntity(json: Json, careerId: Int) =
    ExamDateEntity(id = careerId, data = json.encodeToString(this), cachedAt = getNowMillis())

fun ExamDateEntity.toDomain(json: Json): List<ExamDate> =
    json.decodeFromString<List<ExamDateDto>>(data).map { it.toDomain() }
