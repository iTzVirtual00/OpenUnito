package me.itzvirtual.myunitox.ui.feature_career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val dashboardRepo: DashboardRepository,
    private val examResultRepo: ExamResultRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(CareerScreenState(isGlobalLoading = true))
    val screenState = _screenState.asStateFlow()

    init {
        observeDashboard()
        observeExamResults()
    }

    /**
     * Observes dashboard cache flow. Renders Room data immediately
     * and performs background revalidation if data is absent or stale.
     */
    private fun observeDashboard() {
        viewModelScope.launch {
            // Assumes dashboardRepo exposes dashboardCacheFlow (analogous to studentCacheFlow / CachedGrades)
            dashboardRepo.dashboardCacheFlow.collectLatest { cached ->
                _screenState.update { s ->
                    s.copy(
                        dashboardState = CareerDashboardUiState(
                            data = cached.data,
                            isStale = cached.isStale ?: true,
                            isLoading = cached.data == null,
                            error = null
                        ),
                        // Release global loading as soon as initial cache pass arrives
                        isGlobalLoading = false
                    )
                }

                if (cached.data == null || cached.isStale) {
                    refreshDashboard()
                }
            }
        }
    }

    /**
     * Observes exam results cache flow. Updates UI reactively on every database change.
     */
    private fun observeExamResults() {
        viewModelScope.launch {
            // Assumes examResultRepo exposes examResultsCacheFlow
            examResultRepo.examResultsCacheFlow.collectLatest { cached ->
                val list = cached?.results ?: emptyList()
                _screenState.update { s ->
                    s.copy(
                        examResultsState = ExamResultsUiState(
                            results = list,
                            isStale = cached?.isStale ?: true,
                            isLoading = list.isEmpty(),
                            error = null
                        )
                    )
                }

                if (list.isEmpty() || cached?.isStale == true) {
                    refreshExamResults()
                }
            }
        }
    }

    /**
     * Revalidates dashboard data over the network and saves to Room.
     * Supresses network errors if offline and stale cache is already on screen.
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            dashboardRepo.refresh().onFailure { e ->
                _screenState.update { s ->
                    val shouldSuppress = e is IOException && s.dashboardState.data != null
                    s.copy(
                        dashboardState = s.dashboardState.copy(
                            error = if (shouldSuppress) null else e.message,
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    /**
     * Revalidates exam results over the network and saves to Room.
     */
    fun refreshExamResults() {
        viewModelScope.launch {
            examResultRepo.refresh().onFailure { e ->
                _screenState.update { s ->
                    val shouldSuppress = e is IOException && s.examResultsState.results.isNotEmpty()
                    s.copy(
                        examResultsState = s.examResultsState.copy(
                            error = if (shouldSuppress) null else e.message,
                            isLoading = false
                        )
                    )
                }
            }
        }
    }

    /**
     * Triggered by swipe-to-refresh to sync all modules concurrently.
     */
    fun refreshAll() {
        refreshDashboard()
        refreshExamResults()
    }
}