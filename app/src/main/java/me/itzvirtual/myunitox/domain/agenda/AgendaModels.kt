package me.itzvirtual.myunitox.domain.agenda

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
    fun isToday(): Boolean {
        val today = LocalDate.now()
        val entryDate = startDateTime.atZone(ZoneId.systemDefault()).toLocalDate()
        return entryDate == today
    }

    fun isTomorrow(): Boolean {
        val tomorrow = LocalDate.now().plusDays(1)
        val entryDate = startDateTime.atZone(ZoneId.systemDefault()).toLocalDate()
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
