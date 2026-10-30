package me.itzvirtual.openunito.ui.feature_examsessions

import me.itzvirtual.openunito.domain.examdate.ExamDate

data class ExamDatesUiState(
    val dates: List<ExamDate> = emptyList(),
    val subjects: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ExamDatesScreenState(
    val examDatesState: ExamDatesUiState = ExamDatesUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
    val debug: Boolean? = null,
)
