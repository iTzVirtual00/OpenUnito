package me.itzvirtual.myunitox.ui.transcript

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import me.itzvirtual.myunitox.data.grade.GradeRepository
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class TranscriptViewModel @Inject constructor(
    private val gradeRepo: GradeRepository,
    private val dashboardRepo: DashboardRepository,
) : ViewModel() {
    private val _screenState = MutableStateFlow(TranscriptScreenState())
    val screenState = _screenState.asStateFlow()

    // Experimental: proper Stale-While-Revalidate pattern
    init {
        gradeRepo.gradesFlow
            .onEach { cachedData ->
                _screenState.update { s ->
                    s.copy(
                        gradesState = s.gradesState.copy(
                            grades = cachedData?.grades ?: emptyList(),
                            isStale = cachedData?.isStale ?: true
                        )
                    )
                }
            }
            .launchIn(viewModelScope)

        refreshGrades()
    }

    fun refreshGrades() {
        viewModelScope.launch {
            _screenState.update { s ->
                s.copy(gradesState = s.gradesState.copy(isLoading = true, error = null))
            }

            // val x1Deferred = async { x1Repo.refresh() }
            // val x2Deferred = async { x2Repo.refresh() }

            // val x1Res = x1.await()
            // val x2Res = x2.await()

            gradeRepo.refresh().fold(
                onSuccess = {
                    _screenState.update { s ->
                        s.copy(gradesState = s.gradesState.copy(isLoading = false))
                    }
                },
                onFailure = { e ->
                    _screenState.update { s ->
                        s.copy(
                            gradesState = s.gradesState.copy(
                                // edge case: this shows error if request failed and student has no grades
                                //            contrary to not showing error if request failed and student has grades...
                                error = if (e is IOException && s.gradesState.grades.isNotEmpty()) null else e.message,
                                isLoading = false
                            )
                        )
                    }
                }
            )
        }
    }

    fun changeGradeStatus(examId: String, newStatus: GradeStatus): Job =
        viewModelScope.launch {
            dashboardRepo.changeGradeStatus(examId, newStatus)
                .onSuccess {
                    refreshGrades()
                }
            // TODO: onFailure
        }

}
