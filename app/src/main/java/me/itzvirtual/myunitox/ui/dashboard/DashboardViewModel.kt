package me.itzvirtual.myunitox.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.models.DashboardDataResponse
import me.itzvirtual.myunitox.data.models.ExamGrade
import me.itzvirtual.myunitox.data.models.ExamResult
import me.itzvirtual.myunitox.data.models.GradeResult
import me.itzvirtual.myunitox.data.models.GradeStatus
import me.itzvirtual.myunitox.data.repository.DashboardDataRepository
import me.itzvirtual.myunitox.data.repository.ExamResultRepository
import me.itzvirtual.myunitox.data.repository.GradeRepository
import javax.inject.Inject

data class DashboardData(
    val dashboardData: DashboardDataResponse? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class ExamResultData(
    val examResults: List<ExamResult> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class ExamGradeData(
    val grades: List<ExamGrade> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class DashboardUiState(
    val dashboardData: DashboardData = DashboardData(),
    val examResultsData: ExamResultData = ExamResultData(),
    val examGradesData: ExamGradeData = ExamGradeData(),
    val isLoading: Boolean = true,
)


@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardDataRepo: DashboardDataRepository,
    private val examResultRepo: ExamResultRepository,
    private val examGradeRepository: GradeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // parallel loading!

            val examResultJob = launch {
                examResultRepo.fetchExamResults().onSuccess { results ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            examResultsData = currentState.examResultsData.copy(
                                examResults = results.filter { it.gradeResult!=null }.sortedByDescending { it.date },
                                isLoading = false
                            )
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            examResultsData = it.examResultsData.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        )
                    }
                    Log.e("DashboardViewModel", "Error fetching exam results", error)
                }
            }

            // Section B: Dashboard/Dates
            val dashboardDataJob = launch {
                dashboardDataRepo.fetchDashboardData().onSuccess { data ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            dashboardData = currentState.dashboardData.copy(
                                dashboardData = data,
                                isLoading = false
                            )
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            dashboardData = it.dashboardData.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        )
                    }
                    Log.e("DashboardViewModel", "Error fetching dashboardData", error)
                }
            }

            val examGradesJob = launch {
                examGradeRepository.fetchGrades().onSuccess { data ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            examGradesData = currentState.examGradesData.copy(
                                grades = data.sortedByDescending { it.score?.date },
                                isLoading = false
                            )
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            examGradesData = it.examGradesData.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        )
                    }
                    Log.e("DashboardViewModel", "Error fetching grades", error)
                }
            }


            joinAll(examResultJob, dashboardDataJob, examGradesJob)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun changeGradeStatus(gradeId: String, newStatus: GradeStatus, testing: Boolean = false): Job {
        return viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
            val todo = if (testing) {
                fakeChangeGradeStatus(gradeId, newStatus)
            } else {
                dashboardDataRepo.changeGradeStatus(gradeId, newStatus)
            }

             todo.onSuccess { updatedExamResult ->
                _uiState.update { root ->
                    root.copy(
                        examResultsData = root.examResultsData.copy(
                            examResults = root.examResultsData.examResults.map { examResult ->
                                // for some reason this API doesn't return the whole object back
                                // but rather a partial object
                                if (examResult.id != gradeId) examResult else examResult.copy(
                                    status = updatedExamResult.status
                                )
                            }
                        )
                    )
                }
            }.onFailure { err ->
                Log.e("DashboardViewModel", "Error changing grade status", err)
            }
        }
    }

    suspend fun fakeChangeGradeStatus(gradeId: String, newStatus: GradeStatus): Result<ExamResult> {
        // this is just for testing purposes, it simulates a network call and returns a fake updated ExamResult
        return try {
            delay(3000) // simulate network delay
            val updatedExamResult = ExamResult(
                id = gradeId,
                status = newStatus,

                // not used
                courseName = "Fake Course",
                courseCode = "FAKE101",
                date = "2024-01-01",
                weight = 3,
                editableUntil = "2024-12-31",
                gradeResult = GradeResult(
                    passed = true,
                    grade = "0",
                    isAbsent = false,
                    isWithdrawn = false,
                    canAccept = true,
                    canReject = true,
                ),
                maxGrade = "30",
            )
            Result.success(updatedExamResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}