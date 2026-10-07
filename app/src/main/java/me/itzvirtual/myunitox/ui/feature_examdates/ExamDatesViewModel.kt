package me.itzvirtual.myunitox.ui.feature_examdates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.examdate.ExamDateRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ExamDatesViewModel @Inject constructor(
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(ExamDatesScreenState(isGlobalLoading = true))
    val screenState = _screenState.asStateFlow()

    init {
        observeData()
    }

    /**
     * Combines both cache flows to render Room state instantly.
     * Triggers a background refresh if data is absent or stale.
     */
    private fun observeData() {
        viewModelScope.launch {
            combine(
                examDateRepo.examDatesCacheFlow,
                examResultRepo.examResultsCacheFlow
            ) { datesCache, resultsCache ->
                datesCache to resultsCache
            }.collectLatest { (datesCache, resultsCache) ->
                _screenState.update { current ->
                    current.copy(
                        isGlobalLoading = datesCache.dates == null && resultsCache.results == null,
                        examDatesState = current.examDatesState.copy(
                            dates = datesCache.dates ?: emptyList(),
                            isStale = datesCache.isStale,
                            isLoading = datesCache.dates == null,
                            error = null
                        ),
                        examResultsState = current.examResultsState.copy(
                            results = resultsCache.results ?: emptyList(),
                            isStale = resultsCache.isStale,
                            isLoading = resultsCache.results == null,
                            error = null
                        )
                    )
                }

                // Auto-refresh when either cache is missing or stale
                if (datesCache.dates == null || datesCache.isStale) {
                    refreshExamDates()
                }
                if (resultsCache.results == null || resultsCache.isStale) {
                    refreshExamResults()
                }
            }
        }
    }

    fun refresh() {
        refreshExamDates()
        refreshExamResults()
    }

    private fun refreshExamDates() {
        viewModelScope.launch {
            examDateRepo.refresh().onFailure { err ->
                _screenState.update { current ->
                    val hasCached = current.examDatesState.dates.isNotEmpty()
                    current.copy(
                        isGlobalLoading = false,
                        examDatesState = current.examDatesState.copy(
                            isLoading = false,
                            error = if (err is IOException && hasCached) null else err.message
                        )
                    )
                }
            }
        }
    }

    private fun refreshExamResults() {
        viewModelScope.launch {
            examResultRepo.refresh().onFailure { err ->
                _screenState.update { current ->
                    val hasCached = current.examResultsState.results.isNotEmpty()
                    current.copy(
                        isGlobalLoading = false,
                        examResultsState = current.examResultsState.copy(
                            isLoading = false,
                            error = if (err is IOException && hasCached) null else err.message
                        )
                    )
                }
            }
        }
    }
}