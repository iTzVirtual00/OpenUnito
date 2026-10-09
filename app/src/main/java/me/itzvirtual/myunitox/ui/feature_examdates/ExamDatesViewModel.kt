package me.itzvirtual.myunitox.ui.feature_examdates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.examdate.ExamDateRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import me.itzvirtual.myunitox.ui.feature_career.ExamResultsUiState
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ExamDatesViewModel @Inject constructor(
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
) : ViewModel() {
    private val datesManualError = MutableStateFlow<String?>(null)
    private val resultsManualError = MutableStateFlow<String?>(null)
    private val isManualRefreshing = MutableStateFlow(false)

    val screenState: StateFlow<ExamDatesScreenState> = combine(
        examDateRepo.examDatesCacheFlow,
        examResultRepo.examResultsCacheFlow,
        datesManualError,
        resultsManualError,
        isManualRefreshing
    ) { cachedDates, cachedResults, datesError, resultsError, manualRefreshing ->

        val isDatesLoading = cachedDates == null
        val isResultsLoading = cachedResults == null

        val examDatesUiState = ExamDatesUiState(
            dates = cachedDates?.dates ?: emptyList(),
            isStale = cachedDates?.isStale ?: false,
            isLoading = isDatesLoading,
            error = datesError
        )

        val examResultsUiState = ExamResultsForDatesUiState(
            results = cachedResults?.results ?: emptyList(),
            isStale = cachedResults?.isStale ?: false,
            isLoading = isResultsLoading,
            error = resultsError
        )

        val isGlobalLoading = (isDatesLoading && isResultsLoading) || manualRefreshing

        ExamDatesScreenState(
            isGlobalLoading = isGlobalLoading,
            examDatesState = examDatesUiState,
            examResultsState = examResultsUiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExamDatesScreenState(isGlobalLoading = true)
    )

    /**
     * Concurrent manual refresh for swipe-to-refresh.
     */
    fun refresh() {
        viewModelScope.launch {
            isManualRefreshing.value = true
            try {
                launch { refreshExamDates() }
                launch { refreshExamResults() }
            } finally {
                isManualRefreshing.value = false
            }
        }
    }

    /**
     * User-initiated refresh for exam dates.
     */
    fun refreshExamDates() {
        viewModelScope.launch {
            datesManualError.value = null
            examDateRepo.refresh().onFailure { err ->
                val hasCached = screenState.value.examDatesState.dates.isNotEmpty()
                val shouldSuppress = err is IOException && hasCached
                if (!shouldSuppress) {
                    datesManualError.value = err.message ?: "Failed to refresh exam dates"
                }
            }
        }
    }

    /**
     * User-initiated refresh for exam results.
     */
    fun refreshExamResults() {
        viewModelScope.launch {
            resultsManualError.value = null
            examResultRepo.refresh().onFailure { err ->
                val hasCached = screenState.value.examResultsState.results.isNotEmpty()
                val shouldSuppress = err is IOException && hasCached
                if (!shouldSuppress) {
                    resultsManualError.value = err.message ?: "Failed to refresh exam results"
                }
            }
        }
    }
}