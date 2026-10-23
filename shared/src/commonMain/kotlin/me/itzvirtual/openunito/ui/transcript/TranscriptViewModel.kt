package me.itzvirtual.openunito.ui.transcript

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.dashboard.DashboardRepository
import me.itzvirtual.openunito.data.grade.GradeRepository
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import org.koin.android.annotation.KoinViewModel

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

    fun changeGradeStatus(examId: String, newStatus: GradeStatus): Job =
        viewModelScope.launch {
            manualError.value = null
            dashboardRepo.changeGradeStatus(examId, newStatus)
                .onFailure { e ->
                    manualError.value = e.message ?: "Failed to update grade status"
                }
        }
}
