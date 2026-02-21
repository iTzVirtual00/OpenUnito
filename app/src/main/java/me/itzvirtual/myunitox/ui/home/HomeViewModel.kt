package me.itzvirtual.myunitox.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.StudentData
import me.itzvirtual.myunitox.data.repository.AgendaRepository
import me.itzvirtual.myunitox.data.repository.StudentDataRepository
import javax.inject.Inject

data class TodaysClassesState(
    val todaysClasses: List<AgendaEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class StudentDataState(
    val studentData: StudentData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class UiState(
    val studentDataState: StudentDataState = StudentDataState(),
    val todaysClassesState: TodaysClassesState = TodaysClassesState(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentDataRepo: StudentDataRepository,
    private val agendaRepo: AgendaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    studentDataState = it.studentDataState.copy(
                        isLoading = true,
                        errorMessage = null
                    ),
                    todaysClassesState = it.todaysClassesState.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                )
            }
            val studentDataDeferred = async { studentDataRepo.getStudentData() }
            val agendaDataDeferred = async { agendaRepo.fetchTodayAndTomorrowAgenda() }

            val studentData = studentDataDeferred.await() // TODO: handle errors

            _uiState.update {
                it.copy(
                    studentDataState = it.studentDataState.copy(
                        studentData = studentData,
                        isLoading = false,
                        errorMessage = null
                    )
                )
            }

            val agendaData = agendaDataDeferred.await()

            agendaData.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        todaysClassesState = it.todaysClassesState.copy(
                            todaysClasses = data.filter { entry -> entry.isToday() },
                            isLoading = false,
                            errorMessage = null
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        todaysClassesState = it.todaysClassesState.copy(
                            isLoading = false,
                            errorMessage = "Failed to load today's classes: ${err.message}"
                        )
                    )
                }
            }
        }
    }
}