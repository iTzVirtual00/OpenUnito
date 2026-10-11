package me.itzvirtual.myunitox.ui.feature_examdates

import me.itzvirtual.myunitox.domain.examdate.ExamDate

data class ExamDatesUiState(
    val dates: List<ExamDate> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ExamDatesScreenState(
    val examDatesState: ExamDatesUiState = ExamDatesUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
    val debug: Boolean = false,
)
