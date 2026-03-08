package me.itzvirtual.myunitox.ui.feature_dashboard

import me.itzvirtual.myunitox.domain.dashboard.DashboardData
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.grade.ExamGrade
import me.itzvirtual.myunitox.ui.feature_home.StudentDataUiState

data class DashboardUiState(
    val data: DashboardData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

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

data class DashboardScreenState(
    val studentState: StudentDataUiState = StudentDataUiState(),
    val dashboardState: DashboardUiState = DashboardUiState(isLoading = true),
    val examResultsState: ExamResultsUiState = ExamResultsUiState(isLoading = true),
    val gradesState: GradesUiState = GradesUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
)
