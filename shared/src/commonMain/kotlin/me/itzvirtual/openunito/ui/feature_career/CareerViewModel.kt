package me.itzvirtual.openunito.ui.feature_career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.core.network.isNetworkError
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.dashboard.DashboardRepository
import me.itzvirtual.openunito.data.examresult.ExamResultRepository
import org.koin.android.annotation.KoinViewModel

inline fun <T1, T2, T3, T4, T5, T6, R> uberCombine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>, // Scalable!!
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> {
    return kotlinx.coroutines.flow.combine(
        flow,
        flow2,
        flow3,
        flow4,
        flow5,
        flow6
    ) { args: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
        )
    }
}

@KoinViewModel
class CareerViewModel(
    private val dashboardRepo: DashboardRepository,
    private val examResultRepo: ExamResultRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {

    // Overrides for manual refresh errors (e.g. pull-to-refresh network failures)
    private val dashboardManualError = MutableStateFlow<String?>(null)
    private val examResultsManualError = MutableStateFlow<String?>(null)
    private val isManualRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<CareerScreenState> = uberCombine(
        dashboardRepo.dashboardCacheFlow,
        examResultRepo.examResultsCacheFlow,
        dashboardManualError,
        examResultsManualError,
        isManualRefreshing,
        appConfigRepo.config
    ) { cachedDashboard, cachedExamResults, dashError, examError, manualRefreshing, config ->

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
            isGlobalLoading = isGlobalLoading,
            debug = config?.debug ?: false
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
                val shouldSuppress = e.isNetworkError() && hasData
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
                val shouldSuppress = e.isNetworkError() && hasData
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
