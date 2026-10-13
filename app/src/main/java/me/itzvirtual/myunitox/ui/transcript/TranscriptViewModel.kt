package me.itzvirtual.myunitox.ui.transcript

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.grade.GradeRepository
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import org.koin.android.annotation.KoinViewModel
import java.io.IOException

@KoinViewModel
class TranscriptViewModel(
    private val gradeRepo: GradeRepository,
    private val dashboardRepo: DashboardRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {

    private val manualError = MutableStateFlow<String?>(null)
    private val isManualRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<TranscriptScreenState> = combine(
        gradeRepo.gradesFlow,
        manualError,
        isManualRefreshing,
        appConfigRepo.config
    ) { cachedData, error, manualRefreshing, config ->
        val isLoading = cachedData == null || manualRefreshing

        TranscriptScreenState(
            gradesState = GradesUiState(
                grades = cachedData?.grades ?: emptyList(),
                isStale = cachedData?.isStale ?: false,
                isLoading = isLoading,
                error = error
            ),
            debug = config?.debug ?: false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TranscriptScreenState(
            gradesState = GradesUiState(isLoading = true),
        )
    )

    fun refreshGrades() {
        viewModelScope.launch {
            manualError.value = null
            isManualRefreshing.value = true
            try {
                gradeRepo.refresh().onFailure { e ->
                    val hasCachedGrades = screenState.value.gradesState.grades.isNotEmpty()
                    val shouldSuppress = e is IOException && hasCachedGrades
                    if (!shouldSuppress) {
                        manualError.value = e.message ?: "Failed to refresh grades"
                    }
                }
            } finally {
                isManualRefreshing.value = false
            }
        }
    }

    fun changeGradeStatus(examId: String, newStatus: GradeStatus): Job =
        viewModelScope.launch {
            manualError.value = null
            dashboardRepo.changeGradeStatus(examId, newStatus)
                .onSuccess {
                    refreshGrades()
                }
                .onFailure { e ->
                    manualError.value = e.message ?: "Failed to update grade status"
                }
        }
}
