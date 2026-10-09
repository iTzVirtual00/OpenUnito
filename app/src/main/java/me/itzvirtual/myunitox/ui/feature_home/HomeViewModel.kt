package me.itzvirtual.myunitox.ui.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.calendar.CalendarConfigRepository
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.student.StudentRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepo: StudentRepository,
    private val agendaRepo: AgendaRepository,
    private val calendarConfigRepo: CalendarConfigRepository,
) : ViewModel() {

    private val studentUiStateFlow = studentRepo.studentCacheFlow.map { cached ->
        StudentDataUiState(
            data = cached?.data,
            isLoading = cached?.data == null,
            error = null
        )
    }

    private val agendaUiStateFlow = calendarConfigRepo.selectedCalendarFlow
        .distinctUntilChangedBy { it?.calendarLinkId }
        .flatMapLatest { selectedCalendar ->
            if (selectedCalendar == null) {
                flowOf(
                    HomeAgendaUiState(
                        todaysClasses = DailyClassesUiState(isLoading = false, error = "NO_CALENDAR"),
                        tomorrowClasses = DailyClassesUiState(isLoading = false, error = "NO_CALENDAR")
                    )
                )
            } else {
                val today = LocalDate.now()
                val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endOfTomorrow = today.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()

                agendaRepo.getAgendaFlow(
                    calendarLinkId = selectedCalendar.calendarLinkId,
                    startDate = startOfToday,
                    endDate = endOfTomorrow
                ).map { cachedAgenda ->
                    val hasCachedData = cachedAgenda != null
                    val entries = cachedAgenda?.entries ?: emptyList()

                    HomeAgendaUiState(
                        todaysClasses = DailyClassesUiState(
                            classes = entries.filter { it.isToday() },
                            isLoading = !hasCachedData,
                            error = null
                        ),
                        tomorrowClasses = DailyClassesUiState(
                            classes = entries.filter { it.isTomorrow() },
                            isLoading = !hasCachedData,
                            error = null
                        )
                    )
                }
            }
        }

    val screenState: StateFlow<HomeScreenState> = combine(
        studentUiStateFlow,
        agendaUiStateFlow
    ) { studentState, agendaState ->
        HomeScreenState(
            studentState = studentState,
            todaysClasses = agendaState.todaysClasses,
            tomorrowClasses = agendaState.tomorrowClasses,
            isGlobalLoading = studentState.isLoading && agendaState.todaysClasses.isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenState(isGlobalLoading = true)
    )

    private data class HomeAgendaUiState(
        val todaysClasses: DailyClassesUiState,
        val tomorrowClasses: DailyClassesUiState
    )
}