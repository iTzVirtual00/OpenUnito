package me.itzvirtual.myunitox.ui.feature_career

import me.itzvirtual.myunitox.domain.dashboard.DashboardData

data class CareerDashboardUiState(
    val data: DashboardData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class CareerScreenState(
    val dashboardState: CareerDashboardUiState = CareerDashboardUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
)
