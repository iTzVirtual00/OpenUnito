package me.itzvirtual.openunito.ui.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import me.itzvirtual.openunito.data.agenda.AgendaRepository
import me.itzvirtual.openunito.data.calendar.CalendarConfigRepository
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.student.StudentRepository
import me.itzvirtual.openunito.domain.getCurrentTimeZone
import me.itzvirtual.openunito.domain.getToday
import org.koin.core.annotation.KoinViewModel
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class HomeViewModel(
    private val studentRepo: StudentRepository,
    private val agendaRepo: AgendaRepository,
    private val calendarConfigRepo: CalendarConfigRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {

    private val studentUiStateFlow = studentRepo.studentCacheFlow.map { cached ->
        StudentDataUiState(
            data = cached?.data,
            isLoading = cached?.data == null,
            error = null
        )
    }

    //Pulls uiState from agenda, through a Flow, for today's and tomorrow's classes section
    private val agendaUiStateFlow = calendarConfigRepo.selectedCalendarFlow
        .distinctUntilChangedBy { it?.calendarLinkId }
        .flatMapLatest { selectedCalendar ->
            if (selectedCalendar == null) {
                flowOf(
                    HomeAgendaUiState(
                        todaysClasses = DailyClassesUiState(
                            isLoading = false,
                            error = "NO_CALENDAR"
                        ),
                        tomorrowClasses = DailyClassesUiState(
                            isLoading = false,
                            error = "NO_CALENDAR"
                        )
                    )
                )
            } else {
                val today = getToday()
                val startOfToday = today.atStartOfDayIn(getCurrentTimeZone())
                val endOfTomorrow = today.plus(2, DateTimeUnit.DAY).atStartOfDayIn(getCurrentTimeZone())

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
        agendaUiStateFlow,
        appConfigRepo.config,
    ) { studentState, agendaState, config ->
        HomeScreenState(
            studentState = studentState,
            todaysClasses = agendaState.todaysClasses,
            tomorrowClasses = agendaState.tomorrowClasses,
            isGlobalLoading = studentState.isLoading && agendaState.todaysClasses.isLoading,
            debug = config?.debug ?: false
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
