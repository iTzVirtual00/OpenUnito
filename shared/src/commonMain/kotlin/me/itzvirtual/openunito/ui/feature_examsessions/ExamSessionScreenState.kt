package me.itzvirtual.openunito.ui.feature_examsessions

import me.itzvirtual.openunito.domain.examdate.ExamDate

data class ExamDatesUiState(
    val dates: HashMap<String, List<ExamDate>> = HashMap<String, List<ExamDate>>(),  // Outer list contains the subject("ad" in Api),
    val isLoading: Boolean = false,                 // inner contains the single session
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ExamDatesScreenState(
    val examDatesState: ExamDatesUiState = ExamDatesUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
    val debug: Boolean = false,
)
