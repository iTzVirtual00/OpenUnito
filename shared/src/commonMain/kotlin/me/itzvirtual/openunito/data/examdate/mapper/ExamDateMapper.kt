package me.itzvirtual.openunito.data.examdate.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.data.examdate.local.ExamDateEntity
import me.itzvirtual.openunito.data.examdate.remote.ExamDateDto
import me.itzvirtual.openunito.domain.examdate.ExamBookingState
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.getNowMillis

fun ExamDateDto.toDomain() = ExamDate(
    bookingState = ExamBookingState.BOOKABLE, // TODO: change this shit
    addedToCalendar = false, // TODO: and also this
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

    bookableExam = bookableExam,
    bookingEnabled = bookingEnabled,
    unbookingEnabled = unbookingEnabled,

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
