package me.itzvirtual.myunitox.ui.feature_home

import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import me.itzvirtual.myunitox.domain.student.StudentData

data class StudentDataUiState(
    val data: StudentData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class DailyClassesUiState(
    val classes: List<AgendaEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class HomeScreenState(
    val studentState: StudentDataUiState = StudentDataUiState(),
    val todaysClasses: DailyClassesUiState = DailyClassesUiState(),
    val tomorrowClasses: DailyClassesUiState = DailyClassesUiState(),
    val isGlobalLoading: Boolean = true,
    val debug: Boolean = false,
)
