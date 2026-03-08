package me.itzvirtual.myunitox.ui.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val agendaRepo: AgendaRepository,
    private val appConfigRepo: AppConfigRepository,
    @ApplicationContext private val context: android.content.Context,
) : ViewModel() {

    private val _screenState = MutableStateFlow(HomeScreenState(isGlobalLoading = true))
    val screenState = _screenState.asStateFlow()

    init {
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            _screenState.update { it.copy(isGlobalLoading = true) }

            val studentDeferred = async {
                try {
                    studentRepo.getStudentData()
                } catch (e: Exception) {
                    null to e.message
                }
            }

            val agendaDeferred = async {
                val calendarLinkId = appConfigRepo.getCalendarLinkId()
                val calendarClientId = appConfigRepo.getCalendarClientId()
                if (calendarLinkId == null) {
                    return@async Result.failure<AgendaEntry>(
                        IllegalStateException("MISSING_ID")
                    ).let { Result.failure(IllegalStateException("MISSING_ID")) }
                }
                agendaRepo.fetchTodayAndTomorrow(calendarLinkId, calendarClientId)
            }

            val studentResult = studentDeferred.await()
            val studentData = if (studentResult is Pair<*, *>) null
            else try {
                studentRepo.getStudentData()
            } catch (_: Exception) {
                null
            }

            _screenState.update {
                it.copy(
                    studentState = StudentDataUiState(
                        data = studentData,
                        isLoading = false,
                        error = null,
                    )
                )
            }

            val agendaResult = agendaDeferred.await()
            agendaResult.fold(
                onSuccess = { data ->
                    _screenState.update {
                        it.copy(
                            todaysClasses = DailyClassesUiState(
                                classes = data.filter { e -> e.isToday() },
                                isLoading = false,
                            ),
                            tomorrowClasses = DailyClassesUiState(
                                classes = data.filter { e -> e.isTomorrow() },
                                isLoading = false,
                            ),
                            isGlobalLoading = false,
                        )
                    }
                },
                onFailure = { err ->
                    _screenState.update {
                        it.copy(
                            todaysClasses = DailyClassesUiState(
                                isLoading = false,
                                error = err.message
                            ),
                            tomorrowClasses = DailyClassesUiState(
                                isLoading = false,
                                error = err.message
                            ),
                            isGlobalLoading = false,
                        )
                    }
                }
            )
        }
    }
}
