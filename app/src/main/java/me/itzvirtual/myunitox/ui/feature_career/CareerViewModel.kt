package me.itzvirtual.myunitox.ui.feature_career

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
import me.itzvirtual.myunitox.data.student.StudentRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val dashboardRepo: DashboardRepository,
    private val examResultRepo: ExamResultRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(CareerScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _screenState.update { it.copy(isGlobalLoading = true) }

            // MAY BE USEFUL
            val studentDeferred = async {
                try {
                    studentRepo.getStudentData()
                } catch (_: Exception) {
                    null
                }
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

            val dashboardDeferred = async {
                val cached = dashboardRepo.getCached()
                val isStale = dashboardRepo.isCacheStale()
                if (cached != null) {
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = CareerDashboardUiState(
                                data = cached,
                                isStale = isStale
                            )
                        )
                    }
                }
                dashboardRepo.refresh()
            }

            dashboardDeferred.await().fold(
                onSuccess = {
                    val fresh = dashboardRepo.getCached()
                    _screenState.update { s -> s.copy(dashboardState = CareerDashboardUiState(data = fresh)) }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            dashboardState = s.dashboardState.copy(
                                error = if (e is IOException && s.dashboardState.data != null) null else e.message,
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
                                error = if (e is IOException && s.examResultsState.results.isNotEmpty()) null else e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )

            _screenState.update { it.copy(isGlobalLoading = false) }
        }
    }
}
