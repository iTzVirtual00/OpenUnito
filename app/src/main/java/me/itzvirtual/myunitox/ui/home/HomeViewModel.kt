package me.itzvirtual.myunitox.ui.home

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.data.local.SharedPrefStorage
import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.StudentData
import me.itzvirtual.myunitox.data.repository.AgendaRepository
import me.itzvirtual.myunitox.data.repository.StudentDataRepository
import javax.inject.Inject

data class DailyClassesState(
    val classes: List<AgendaEntry> = emptyList(),
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
    val todaysClassesState: DailyClassesState = DailyClassesState(),
    val tomorrowClassesState: DailyClassesState = DailyClassesState(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentDataRepo: StudentDataRepository,
    private val agendaRepo: AgendaRepository,
    private val sharedPrefStorage: SharedPrefStorage,
    @ApplicationContext private val context: android.content.Context
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
                    ),
                    tomorrowClassesState = it.tomorrowClassesState.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                )
            }
            val studentDataDeferred = async { studentDataRepo.getStudentData() }
            val agendaDataDeferred = async {
                val calendarLinkId = sharedPrefStorage.getCalendarLinkId()
                val calendarClientId = sharedPrefStorage.getCalendarClientId()
                if (calendarLinkId == null) {
                    return@async Result.failure(
                        IllegalStateException("MISSING_ID")
                    )
                }
                return@async agendaRepo.fetchTodayAndTomorrowAgenda(calendarLinkId, calendarClientId)
            }

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
                            classes = data.filter { entry -> entry.isToday() },
                            isLoading = false,
                            errorMessage = null
                        ),
                        tomorrowClassesState = it.todaysClassesState.copy(
                            classes = data.filter { entry -> entry.isTomorrow() },
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
                            errorMessage = err.message
                        ),
                        tomorrowClassesState = it.todaysClassesState.copy(
                            isLoading = false,
                            errorMessage = err.message
                        )
                    )
                }
            }
        }
    }
}