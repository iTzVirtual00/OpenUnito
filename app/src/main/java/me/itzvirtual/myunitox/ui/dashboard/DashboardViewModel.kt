package me.itzvirtual.myunitox.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.repository.DashboardDataRepository
import me.itzvirtual.myunitox.ui.home.HomeUiState
import javax.inject.Inject

data class DashboardUiState(
    val dashboardData: DashboardDataResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardDataRepo: DashboardDataRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val dashboardDataDeferred = async { dashboardDataRepo.fetchDashboardData() }

            val dashboardData = dashboardDataDeferred.await().apply {
                onFailure { error ->
                    Log.e("UnitoAPI", "Mapping failed for dashboardData: ${error.message}")
                }
            }

            _uiState.value = DashboardUiState(
                dashboardData = dashboardData.getOrNull(),
                isLoading = false
            )
        }
    }
}