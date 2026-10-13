package me.itzvirtual.myunitox.ui.feature_examsessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.examdate.ExamDateRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import me.itzvirtual.myunitox.domain.examdate.ExamDate
import me.itzvirtual.myunitox.ui.feature_career.uberCombine
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ExamSessionViewModel @Inject constructor(
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {
    private val datesManualError = MutableStateFlow<String?>(null)
    private val resultsManualError = MutableStateFlow<String?>(null)
    private val isManualRefreshing = MutableStateFlow(false)

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

        var datesMap: HashMap<String, List<ExamDate>> = HashMap<String, List<ExamDate>>()
        val sortedDates = cachedDates?.dates?.sortedByDescending { it.title } ?: emptyList()
        sortedDates.forEach { date ->
            val title = date.title

            val el = datesMap.putIfAbsent(
                title,
                List(1) {
                    date
                }
            )

            if (el != null) {
                el.plus(date)
            }
        }

        val examDatesUiState = ExamDatesUiState(
            dates = datesMap,
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
    fun refresh() {
        viewModelScope.launch {
            isManualRefreshing.value = true
            try {
                launch { refreshExamDates() }
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
}