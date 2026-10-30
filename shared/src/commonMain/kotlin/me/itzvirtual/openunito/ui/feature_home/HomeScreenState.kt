package me.itzvirtual.openunito.ui.feature_home

import me.itzvirtual.openunito.domain.agenda.ActivityType
import me.itzvirtual.openunito.domain.agenda.AgendaEntry
import me.itzvirtual.openunito.domain.agenda.Professor
import me.itzvirtual.openunito.domain.agenda.RoomLocation
import me.itzvirtual.openunito.domain.student.StudentData
import kotlin.time.Instant

data class StudentDataUiState(
    val data: StudentData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class DailyClassesUiState(
    val classes: List<AgendaEntry> = emptyList(), // TODO: Mario make this list?
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class HomeScreenState(
    val studentState: StudentDataUiState = StudentDataUiState(),
    val todaysClasses: DailyClassesUiState = DailyClassesUiState(),
    val tomorrowClasses: DailyClassesUiState = DailyClassesUiState(),
    val isGlobalLoading: Boolean = true,
    val showAttendance: Boolean = true,
    val debug: Boolean = false,
)

val todaysClassesSample = listOf(
    AgendaEntry(
        id = "0",
        title = "Digital Thinking",
        startDateTime = Instant.parse("2026-02-22T06:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-22T08:00:00.000Z"),
        activityCode = "DT001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room1", code = "MA", name = "Main auditorium")
        ),
        professors = listOf(
            Professor("prof1", "Elena", "Galli")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "1",
        title = "Digital Thinking",
        startDateTime = Instant.parse("2026-02-22T08:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-22T10:00:00.000Z"),
        activityCode = "DT001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room1", code = "MA", name = "Main auditorium")
        ),
        professors = listOf(
            Professor("prof1", "Elena", "Galli")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "2",
        title = "Computer Science",
        startDateTime = Instant.parse("2026-02-22T11:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-22T13:00:00.000Z"),
        activityCode = "CS001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room2", code = "A2", name = "Aula 2")
        ),
        professors = listOf(
            Professor("prof2", "Marco", "Rossi")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "2",
        title = "Kitchen",
        startDateTime = Instant.parse("2026-02-22T19:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-22T23:30:00.000Z"),
        activityCode = "CS001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room2", code = "A2", name = "Aula 2")
        ),
        professors = listOf(
            Professor("prof2", "Marco", "Rossi")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "2",
        title = "Sleep",
        startDateTime = Instant.parse("2026-02-22T23:40:00.000Z"),
        endDateTime = Instant.parse("2026-02-23T07:40:00.000Z"),
        activityCode = "CS001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room2", code = "A2", name = "Aula 2")
        ),
        professors = listOf(
            Professor("prof2", "Marco", "Rossi")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
)
val tomorrowClassesSample = listOf(
    AgendaEntry(
        id = "3",
        title = "Mathematics",
        startDateTime = Instant.parse("2026-02-23T08:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-23T10:00:00.000Z"),
        activityCode = "MATH001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room3", code = "B1", name = "Aula B1")
        ),
        professors = listOf(
            Professor("prof3", "Luca", "Bianchi")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "3",
        title = "Mathematics",
        startDateTime = Instant.parse("2026-02-23T10:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-23T12:00:00.000Z"),
        activityCode = "MATH001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            RoomLocation(id = "room3", code = "B1", name = "Aula B1")
        ),
        professors = listOf(
            Professor("prof3", "Luca", "Bianchi")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
)