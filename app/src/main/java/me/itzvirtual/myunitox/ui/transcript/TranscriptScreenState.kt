package me.itzvirtual.myunitox.ui.transcript

import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.grade.ExamGrade


data class ExamResultsUiState(
    val results: List<ExamResult> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class GradesUiState(
    val grades: List<ExamGrade> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class TranscriptScreenState(
    val examResultsState: ExamResultsUiState = ExamResultsUiState(isLoading = true),
    val gradesState: GradesUiState = GradesUiState(isLoading = true),
)