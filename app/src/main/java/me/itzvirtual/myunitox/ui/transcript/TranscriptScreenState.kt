package me.itzvirtual.myunitox.ui.transcript

import me.itzvirtual.myunitox.domain.grade.ExamGrade

data class GradesUiState(
    val grades: List<ExamGrade> = emptyList(), // what about null = not ready?
    val isLoading: Boolean = true,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class TranscriptScreenState(
    val gradesState: GradesUiState = GradesUiState(isLoading = true),
    val debug: Boolean = false,
)