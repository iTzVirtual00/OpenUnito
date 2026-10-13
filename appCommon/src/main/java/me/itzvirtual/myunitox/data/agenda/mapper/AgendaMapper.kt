package me.itzvirtual.myunitox.data.agenda.mapper

import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.data.agenda.local.AgendaEntity
import me.itzvirtual.myunitox.data.agenda.remote.ActivityTypeDto
import me.itzvirtual.myunitox.data.agenda.remote.AgendaEntryDto
import me.itzvirtual.myunitox.data.agenda.remote.BuildingDto
import me.itzvirtual.myunitox.data.agenda.remote.CoordinatesDto
import me.itzvirtual.myunitox.data.agenda.remote.FloorDto
import me.itzvirtual.myunitox.data.agenda.remote.ProfessorDto
import me.itzvirtual.myunitox.data.agenda.remote.RoomLocationDto
import me.itzvirtual.myunitox.domain.agenda.ActivityType
import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import me.itzvirtual.myunitox.domain.agenda.Building
import me.itzvirtual.myunitox.domain.agenda.Coordinates
import me.itzvirtual.myunitox.domain.agenda.Floor
import me.itzvirtual.myunitox.domain.agenda.Professor
import me.itzvirtual.myunitox.domain.agenda.RoomLocation

fun AgendaEntryDto.toDomain() = AgendaEntry(
    id = id,
    title = title,
    titleEn = titleEn,
    startDateTime = startDateTime,
    endDateTime = endDateTime,
    activityCode = activityCode,
    courseYear = courseYear,
    status = status,
    rooms = rooms.map { it.toDomain() },
    professors = professors.map { it.toDomain() },
    activityType = activityType.toDomain(),
    isRemote = isRemote,
    isUnavailable = isUnavailable,
)

fun RoomLocationDto.toDomain() = RoomLocation(
    id = id, code = code, name = name, capacity = capacity,
    photoUrl = photoUrl, floorPlanUrl = floorPlanUrl,
    building = building?.toDomain(), floor = floor?.toDomain(),
)

fun BuildingDto.toDomain() = Building(
    id = id, name = name, address = address,
    streetNumber = streetNumber, city = city, coordinates = coordinates?.toDomain(),
)

fun ProfessorDto.toDomain() = Professor(id = id, firstName = firstName, lastName = lastName)

fun ActivityTypeDto.toDomain() = ActivityType(code = code, description = description)

fun FloorDto.toDomain() = Floor(code = code, description = description)

fun CoordinatesDto.toDomain() = Coordinates(latitude = latitude, longitude = longitude)

fun List<AgendaEntryDto>.toEntity(key: String, json: Json): AgendaEntity =
    AgendaEntity(
        dateRangeKey = key,
        data = json.encodeToString(this),
        cachedAt = System.currentTimeMillis()
    )

fun AgendaEntity.toDomain(json: Json): List<AgendaEntry> =
    json.decodeFromString<List<AgendaEntryDto>>(data).map { it.toDomain() }
