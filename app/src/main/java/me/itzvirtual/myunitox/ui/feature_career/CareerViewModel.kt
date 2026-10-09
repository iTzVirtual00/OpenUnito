package me.itzvirtual.myunitox.ui.feature_career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val dashboardRepo: DashboardRepository,
    private val examResultRepo: ExamResultRepository,
) : ViewModel() {

    // Overrides for manual refresh errors (e.g. pull-to-refresh network failures)
    private val dashboardManualError = MutableStateFlow<String?>(null)
    private val examResultsManualError = MutableStateFlow<String?>(null)
    private val isManualRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<CareerScreenState> = combine(
        dashboardRepo.dashboardCacheFlow,
        examResultRepo.examResultsCacheFlow,
        dashboardManualError,
        examResultsManualError,
        isManualRefreshing
    ) { cachedDashboard, cachedExamResults, dashError, examError, manualRefreshing ->

        val isDashboardLoading = cachedDashboard == null
        val isExamResultsLoading = cachedExamResults == null

        val dashboardUiState = CareerDashboardUiState(
            data = cachedDashboard?.data,
            isStale = cachedDashboard?.isStale ?: false,
            isLoading = isDashboardLoading,
            error = dashError
        )

        val examResultsUiState = ExamResultsUiState(
            results = cachedExamResults?.results ?: emptyList(),
            isStale = cachedExamResults?.isStale ?: false,
            isLoading = isExamResultsLoading,
            error = examError
        )

        val isGlobalLoading = (isDashboardLoading && isExamResultsLoading) || manualRefreshing

        CareerScreenState(
            dashboardState = dashboardUiState,
            examResultsState = examResultsUiState,
            isGlobalLoading = isGlobalLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CareerScreenState(isGlobalLoading = true)
    )

    // experimental pull-to-refresh features
    fun refreshDashboard() {
        viewModelScope.launch {
            dashboardManualError.value = null
            dashboardRepo.refresh().onFailure { e ->
                val hasData = screenState.value.dashboardState.data != null
                val shouldSuppress = e is IOException && hasData
                if (!shouldSuppress) {
                    dashboardManualError.value = e.message ?: "Failed to refresh dashboard"
                }
            }
        }
    }

    fun refreshExamResults() {
        viewModelScope.launch {
            examResultsManualError.value = null
            examResultRepo.refresh().onFailure { e ->
                val hasData = screenState.value.examResultsState.results.isNotEmpty()
                val shouldSuppress = e is IOException && hasData
                if (!shouldSuppress) {
                    examResultsManualError.value = e.message ?: "Failed to refresh exam results"
                }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            isManualRefreshing.value = true
            try {
                launch { refreshDashboard() }
                launch { refreshExamResults() }
            } finally {
                isManualRefreshing.value = false
            }
        }
    }
}