package me.itzvirtual.myunitox.ui.feature_profile

import me.itzvirtual.myunitox.domain.dashboard.DashboardData
import me.itzvirtual.myunitox.domain.student.StudentData

data class ProfileUiState(
    val data: StudentData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ProfileDashboardUiState(
    val data: DashboardData? = null,
    val isLoading: Boolean = false,
    val isStale: Boolean = false,
    val error: String? = null,
)

data class ProfileScreenState(
    val profileState: ProfileUiState = ProfileUiState(isLoading = true),
    val dashboardState: ProfileDashboardUiState = ProfileDashboardUiState(isLoading = true),
    val isGlobalLoading: Boolean = true,
)
