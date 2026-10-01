package me.itzvirtual.myunitox.ui.transcript

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val examResultRepo: ExamResultRepository,
    private val gradeRepo: GradeRepository,
    private val dashboardRepo: DashboardRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(TranscriptScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
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


            gradeDeferred.await().fold(
                onSuccess = {
                    val fresh = gradeRepo.getCached()?.sortedByDescending {
                        it.score?.date
                    }
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
                .onSuccess { load() }
        }
}
