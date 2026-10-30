package me.itzvirtual.openunito.ui.feature_career

import me.itzvirtual.openunito.domain.dashboard.DashboardData
import me.itzvirtual.openunito.domain.examresult.ExamResult

data class CareerDashboardUiState(
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

data class CareerScreenState(
    val dashboardState: CareerDashboardUiState = CareerDashboardUiState(isLoading = true),
    val examResultsState: ExamResultsUiState = ExamResultsUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
    val debug: Boolean? = false,
)
