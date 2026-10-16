package me.itzvirtual.openunito.domain.agenda

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class AgendaEntry(
    val id: String,
    val title: String,
    val titleEn: String? = null,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val activityCode: String,
    val courseYear: Int,
    val status: String,
    val rooms: List<RoomLocation>,
    val professors: List<Professor>,
    val activityType: ActivityType,
    val isRemote: Boolean = false,
    val isUnavailable: Boolean = false,
) {
    fun isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val entryDate = startDateTime.toLocalDateTime(timeZone).date
        return entryDate == today
    }

    fun isTomorrow(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val tomorrow = today.plus(1, DateTimeUnit.DAY)
        val entryDate = startDateTime.toLocalDateTime(timeZone).date
        return entryDate == tomorrow
    }
}

data class RoomLocation(
    val id: String,
    val code: String,
    val name: String,
    val capacity: Int? = null,
    val photoUrl: String? = null,
    val floorPlanUrl: String? = null,
    val building: Building? = null,
    val floor: Floor? = null,
)

data class Building(
    val id: String,
    val name: String,
    val address: String?,
    val streetNumber: String?,
    val city: String?,
    val coordinates: Coordinates?,
)

data class Professor(
    val id: String,
    val firstName: String,
    val lastName: String,
) {
    val fullName: String get() = "$firstName $lastName"
}

data class ActivityType(
    val code: String,
    val description: String,
)

data class Floor(
    val code: String,
    val description: String,
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)
