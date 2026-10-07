package me.itzvirtual.myunitox.ui.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.calendar.CalendarConfigRepository
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfig
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val agendaRepo: AgendaRepository,
    private val appConfigRepo: AppConfigRepository,
    private val calendarConfigRepo: CalendarConfigRepository,
    @ApplicationContext private val context: android.content.Context,
) : ViewModel() {

    private val _screenState = MutableStateFlow(HomeScreenState(isGlobalLoading = true))
    val screenState = _screenState.asStateFlow()

    init {
        observeStudentData()
        observeSelectedCalendar()
    }

    private fun observeStudentData() {
        viewModelScope.launch {
            studentRepo.studentCacheFlow.collectLatest { cached ->
                _screenState.update { state ->
                    state.copy(
                        studentState = StudentDataUiState(
                            data = cached.data,
                            // If we have cached data, don't block the screen with a spinner
                            isLoading = cached.data == null,
                            error = null
                        ),
                        isGlobalLoading = false
                    )
                }

                // If cache is empty or stale, trigger a silent background refresh
                if (cached.data == null || cached.isStale) {
                    refreshStudentData()
                }
            }
        }
    }

    fun refreshStudentData() {
        viewModelScope.launch {
            val result = studentRepo.refresh()
            result.onFailure { err ->
                _screenState.update { state ->
                    // Only surface the error if we have no cached data to show
                    if (state.studentState.data == null) {
                        state.copy(
                            studentState = state.studentState.copy(
                                isLoading = false,
                                error = err.message
                            )
                        )
                    } else {
                        state.copy(
                            studentState = state.studentState.copy(isLoading = false)
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelectedCalendar() {
        viewModelScope.launch {
            calendarConfigRepo.selectedCalendarFlow
                // Prevent refetching if only cosmetic fields (like 'name') change
                .distinctUntilChangedBy { it?.calendarLinkId }
                .collectLatest { selectedCalendar ->
                    loadAgendaForCalendar(selectedCalendar)
                }
        }
    }

    private suspend fun loadAgendaForCalendar(selectedCalendar: CalendarConfig?) {
        if (selectedCalendar == null) {
            _screenState.update {
                it.copy(
                    todaysClasses = DailyClassesUiState(
                        isLoading = false,
                        error = "NO_CALENDAR"
                    ),
                    tomorrowClasses = DailyClassesUiState(
                        isLoading = false,
                        error = "NO_CALENDAR"
                    )
                )
            }
            return
        }

        // Show loading state while fetching classes for the newly selected calendar
        _screenState.update {
            it.copy(
                todaysClasses = it.todaysClasses.copy(isLoading = true, error = null),
                tomorrowClasses = it.tomorrowClasses.copy(isLoading = true, error = null)
            )
        }

        val calendarClientId = appConfigRepo.getCalendarClientId()
        val result = runCatching {
            agendaRepo.fetchTodayAndTomorrow(
                calendarLinkId = selectedCalendar.calendarLinkId,
                clientId = calendarClientId
            ).getOrThrow()
        }

        result.fold(
            onSuccess = { entries ->
                _screenState.update {
                    it.copy(
                        todaysClasses = DailyClassesUiState(
                            classes = entries.filter { e -> e.isToday() },
                            isLoading = false,
                            error = null
                        ),
                        tomorrowClasses = DailyClassesUiState(
                            classes = entries.filter { e -> e.isTomorrow() },
                            isLoading = false,
                            error = null
                        )
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
                        )
                    )
                }
            }
        )
    }
}
