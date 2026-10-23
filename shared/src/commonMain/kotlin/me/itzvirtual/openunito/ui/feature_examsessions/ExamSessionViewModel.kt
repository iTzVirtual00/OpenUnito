package me.itzvirtual.openunito.ui.feature_examsessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.core.network.isNetworkError
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.examdate.ExamDateRepository
import me.itzvirtual.openunito.data.examresult.ExamResultRepository
import me.itzvirtual.openunito.ui.feature_career.uberCombine
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ExamSessionViewModel(
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {
    private val datesManualError = MutableStateFlow<String?>(null)
    private val resultsManualError = MutableStateFlow<String?>(null)
    private val isManualRefreshing = MutableStateFlow(false)

    // TODO: refresh behaviour integration and cleanup
    val screenState: StateFlow<ExamDatesScreenState> = uberCombine(
        examDateRepo.examDatesCacheFlow,
        examResultRepo.examResultsCacheFlow,
        datesManualError,
        resultsManualError,
        isManualRefreshing,
        appConfigRepo.config
    ) { cachedDates, cachedResults, datesError, resultsError, manualRefreshing, config ->

        val isDatesLoading = cachedDates == null
        val isResultsLoading = cachedResults == null

        val sortedDates = cachedDates?.dates?.sortedBy { it.date } ?: emptyList()

        val examDatesUiState = ExamDatesUiState(
            dates = sortedDates,
            isStale = cachedDates?.isStale ?: false,
            isLoading = isDatesLoading,
            error = datesError
        )

        val isGlobalLoading = (isDatesLoading && isResultsLoading) || manualRefreshing

        ExamDatesScreenState(
            examDatesState = examDatesUiState,
            isGlobalLoading = isGlobalLoading,
            debug = config?.debug ?: false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExamDatesScreenState(isGlobalLoading = true)
    )

    /**
     * Concurrent manual refresh for swipe-to-refresh.
     */
    fun refresh(careerId: Int) {
        viewModelScope.launch {
            isManualRefreshing.value = true
            try {
                launch { refreshExamDates(careerId) }
            } finally {
                isManualRefreshing.value = false
            }
        }
    }

    /**
     * User-initiated refresh for exam dates.
     */
    fun refreshExamDates(careerId: Int) {
        viewModelScope.launch {
            datesManualError.value = null
            examDateRepo.refresh(careerId).onFailure { err ->
                val hasCached = screenState.value.examDatesState.dates.isNotEmpty()
                val shouldSuppress = err.isNetworkError() && hasCached
                if (!shouldSuppress) {
                    datesManualError.value = err.message ?: "Failed to refresh exam dates"
                }
            }
        }
    }
}
