package me.itzvirtual.myunitox.ui.transcript

import me.itzvirtual.myunitox.domain.grade.ExamGrade

data class GradesUiState(
    val grades: List<ExamGrade> = emptyList(),
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class TranscriptScreenState(
    val gradesState: GradesUiState = GradesUiState(isLoading = true),
)