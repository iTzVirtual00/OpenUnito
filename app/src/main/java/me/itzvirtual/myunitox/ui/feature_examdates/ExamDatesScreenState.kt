package me.itzvirtual.myunitox.ui.feature_examdates

import me.itzvirtual.myunitox.domain.examdate.ExamDate
import me.itzvirtual.myunitox.domain.examresult.ExamResult

data class ExamDatesUiState(
    val dates: List<ExamDate> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ExamResultsForDatesUiState( //Remove it, should be useless
    val results: List<ExamResult> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ExamDatesScreenState(
    val examDatesState: ExamDatesUiState = ExamDatesUiState(isLoading = true),
    val examResultsState: ExamResultsForDatesUiState = ExamResultsForDatesUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
)
