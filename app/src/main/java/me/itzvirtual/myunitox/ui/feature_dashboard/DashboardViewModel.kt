package me.itzvirtual.myunitox.ui.feature_dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import me.itzvirtual.myunitox.data.grade.GradeRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import me.itzvirtual.myunitox.ui.feature_home.StudentDataUiState
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val dashboardRepo: DashboardRepository,
    private val examResultRepo: ExamResultRepository,
    private val gradeRepo: GradeRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(DashboardScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _screenState.update { it.copy(isGlobalLoading = true) }

            val studentDeferred = async {
                try {
                    studentRepo.getStudentData()
                } catch (_: Exception) {
                    null
                }
            }
            val dashboardDeferred = async {
                val cached = dashboardRepo.getCached()
                val isStale = dashboardRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = DashboardUiState(
                                data = cached,
                                isStale = isStale
                            )
                        )
                    }
                }
                dashboardRepo.refresh()
            }
            val examResultDeferred = async {
                val cached = examResultRepo.getCached()
                val isStale = examResultRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(
                            examResultsState = ExamResultsUiState(
                                results = cached,
                                isStale = isStale
                            )
                        )
                    }
                }
                examResultRepo.refresh()
            }
            val gradeDeferred = async {
                val cached = gradeRepo.getCached()
                val isStale = gradeRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(
                            gradesState = GradesUiState(
                                grades = cached,
                                isStale = isStale
                            )
                        )
                    }
                }
                gradeRepo.refresh()
            }

            val student = studentDeferred.await()
            _screenState.update { it.copy(studentState = StudentDataUiState(data = student)) }

            dashboardDeferred.await().fold(
                onSuccess = {
                    val fresh = dashboardRepo.getCached()
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = DashboardUiState(
                                data = fresh,
                                isStale = false
                            )
                        )
                    }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = s.dashboardState.copy(
                                error = e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )

            examResultDeferred.await().fold(
                onSuccess = {
                    val fresh = examResultRepo.getCached()
                    _screenState.update { s ->
                        s.copy(
                            examResultsState = ExamResultsUiState(
                                results = fresh ?: emptyList()
                            )
                        )
                    }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            examResultsState = s.examResultsState.copy(
                                error = e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )

            gradeDeferred.await().fold(
                onSuccess = {
                    val fresh = gradeRepo.getCached()
                    _screenState.update { s ->
                        s.copy(
                            gradesState = GradesUiState(
                                grades = fresh ?: emptyList()
                            )
                        )
                    }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            gradesState = s.gradesState.copy(
                                error = e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )

            _screenState.update { it.copy(isGlobalLoading = false) }
        }
    }

    fun changeGradeStatus(examId: String, newStatus: GradeStatus): kotlinx.coroutines.Job =
        viewModelScope.launch {
            dashboardRepo.changeGradeStatus(examId, newStatus)
                .onSuccess { refresh() }
        }
}
