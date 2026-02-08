package me.itzvirtual.myunitox.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.repository.ExamResultRepository
import kotlinx.coroutines.flow.asStateFlow
import me.itzvirtual.myunitox.data.models.ExamDate
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.repository.ExamDateRepository
import javax.inject.Inject


data class HomeUiState(
    val examResults: List<ExamResult> = emptyList(),
    val examDates: List<ExamDate> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val examResultRepo: ExamResultRepository,
    private val examDatesRepo: ExamDateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val examsResultsDeferred = async { examResultRepo.fetchExamResults() }
            val examDatesDeferred = async { examDatesRepo.fetchExamDates() }

            val examsResult = examsResultsDeferred.await().apply {
                onFailure { error ->
                    Log.e("UnitoAPI", "Mapping failed for examResult: ${error.message}")
                }
            }
            val examDates = examDatesDeferred.await().apply {
                onFailure { error ->
                    Log.e("UnitoAPI", "Mapping failed for examDates" +
                            ": ${error.message}")
                }
            }

            _uiState.value = HomeUiState(
                examResults = examsResult.getOrDefault(emptyList()),
                examDates = examDates.getOrDefault(emptyList()),
                isLoading = false
            )
        }
    }
}