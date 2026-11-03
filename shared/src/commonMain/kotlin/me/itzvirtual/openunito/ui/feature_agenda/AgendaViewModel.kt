package me.itzvirtual.openunito.ui.feature_agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import me.itzvirtual.openunito.data.agenda.AgendaRepository
import me.itzvirtual.openunito.data.calendar.CalendarConfigRepository
import me.itzvirtual.openunito.data.calendar.local.CalendarConfig
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.domain.atStartOfDay
import me.itzvirtual.openunito.domain.getDayOfWeek
import me.itzvirtual.openunito.domain.getToday
import org.koin.android.annotation.KoinViewModel

data class WeekRange(
    val start: LocalDate,
    val end: LocalDate
)

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class AgendaViewModel(
    private val agendaRepository: AgendaRepository,
    private val appConfigRepo: AppConfigRepository,
    private val calendarConfigRepo: CalendarConfigRepository,
) : ViewModel() {

    private val currentWeek = run {
        WeekRange(
            start = getDayOfWeek(DayOfWeek.MONDAY),
            end = getDayOfWeek(DayOfWeek.SUNDAY)
        )
    }

    private val selectedWeekFlow = MutableStateFlow(currentWeek)

    // This only populates AgendaUiData (viewmodel-internal structure)
    // AgendaUiData contains the fetched selected week
    private val agendaDataFlow = combine(
        calendarConfigRepo.selectedCalendarFlow,
        selectedWeekFlow
    ) { selectedCalendar, week ->
        selectedCalendar to week
    }
        .distinctUntilChanged()
        .flatMapLatest { (calendar, week) ->
            if (calendar == null) {
                flowOf(
                    AgendaUiData(
                        week = week,
                        isLoading = false,
                        agendaState = AgendaUiState(
                            agendaEntries = emptyList(),
                            error = "NO_CALENDAR"
                        )
                    )
                )
            } else {
                val startInstant = week.start.atStartOfDay()
                val endInstant =
                    week.end.plus(1, DateTimeUnit.DAY).atStartOfDay()

                agendaRepository.getAgendaFlow(
                    calendarLinkId = calendar.calendarLinkId,
                    startDate = startInstant,
                    endDate = endInstant
                ).map { cachedAgenda ->
                    val hasCachedData = cachedAgenda != null
                    Logger.d(tag = "AgendaViewModel") { "hasCachedData: $hasCachedData" }
                    AgendaUiData(
                        week = week,
                        isLoading = !hasCachedData,
                        agendaState = AgendaUiState(
                            agendaEntries = cachedAgenda?.entries ?: emptyList(),
                            isStale = cachedAgenda?.isStale ?: false,
                            error = null
                        )
                    )
                }
            }
        }

    // TODO:
    //  fix brief loading flash, this is caused by an architectural choice
    //  the repo already eagerly prefetches data for the current week
    //  (with 10 second lifetime caused by a side effect)
    //  The problem lies in `initialState` of ALL the StateFlows in the repo and view models
    //  more research is needed
    val screenState: StateFlow<AgendaScreenState> = combine(
        calendarConfigRepo.calendarsFlow,
        calendarConfigRepo.selectedCalendarFlow,
        agendaDataFlow
    ) { calendars, selectedCalendar, agendaData ->
        AgendaScreenState(
            calendars = calendars,
            selectedCalendar = selectedCalendar,
            // Applies AgendaUiData to the ui state
            selectedStart = agendaData.week.start,
            selectedEnd = agendaData.week.end,
            isLoading = agendaData.isLoading,
            agendaState = agendaData.agendaState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AgendaScreenState(
            selectedStart = currentWeek.start,
            selectedEnd = currentWeek.end,
            isLoading = true
        )
    )

    // --- Navigation ---

    fun onNextWeek() {
        selectWeek(selectedWeekFlow.value.start.plus(1, DateTimeUnit.WEEK))
    }

    fun onPreviousWeek() {
        selectWeek(selectedWeekFlow.value.start.minus(1, DateTimeUnit.WEEK))
    }

    fun goToToday() {
        selectWeek(getToday())
    }

    fun selectWeek(targetDate: LocalDate) {
        val monday = getDayOfWeek(DayOfWeek.MONDAY, today = targetDate)
        val sunday = getDayOfWeek(DayOfWeek.SUNDAY, today = targetDate)
        selectedWeekFlow.value = WeekRange(start = monday, end = sunday)
    }

    // --- Calendar CRUD ---

    fun selectCalendar(calendar: CalendarConfig) {
        viewModelScope.launch {
            calendarConfigRepo.setSelectedCalendarId(calendar.id)
        }
    }

    fun upsertCalendar(
        name: String,
        calendarLinkId: String,
        existingId: Int?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val clientId = appConfigRepo.getCalendarClientId()
            val isValid = agendaRepository.validateCalendarLinkId(calendarLinkId, clientId)
            if (!isValid) {
                onResult(false, "Invalid calendar ID — could not fetch data.")
                return@launch
            }

            val savedCal = if (existingId != null) {
                calendarConfigRepo.edit(existingId, name, calendarLinkId)
            } else {
                calendarConfigRepo.add(name, calendarLinkId)
            }

            calendarConfigRepo.setSelectedCalendarId(savedCal.id)
            onResult(true, "")
        }
    }

    fun deleteCalendar(id: Int) {
        viewModelScope.launch {
            calendarConfigRepo.delete(id)
        }
    }

    private data class AgendaUiData(
        val week: WeekRange,
        val isLoading: Boolean,
        val agendaState: AgendaUiState
    )
}
