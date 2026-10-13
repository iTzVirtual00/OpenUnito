package me.itzvirtual.myunitox.ui.feature_agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.calendar.CalendarConfigRepository
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfig
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import org.koin.android.annotation.KoinViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

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
        val now = LocalDate.now()
        WeekRange(
            start = now.with(DayOfWeek.MONDAY),
            end = now.with(DayOfWeek.SUNDAY)
        )
    }

    private val selectedWeekFlow = MutableStateFlow(currentWeek)

    // This only populates AgendaUiData (internal structure)
    private val agendaDataFlow = combine(
        calendarConfigRepo.selectedCalendarFlow,
        selectedWeekFlow
    ) { selectedCalendar, week ->
        selectedCalendar to week
    }
        .distinctUntilChanged()
        // flatMapLatest automagically cancels the flow we were attached to if the tuple changes.
        // DAMN
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
                val startInstant = week.start.atStartOfDay(ZoneId.systemDefault()).toInstant()
                val endInstant =
                    week.end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

                agendaRepository.getAgendaFlow(
                    calendarLinkId = calendar.calendarLinkId,
                    startDate = startInstant,
                    endDate = endInstant
                ).map { cachedAgenda ->
                    val hasCachedData = cachedAgenda != null
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
        // stateIn is so cool (it's the single collector of the above flow,
        // and it emits the latest value when someone subscribes
        // (otherwise the above flow will run again every time someone subscribes)
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
        selectWeek(selectedWeekFlow.value.start.plusWeeks(1))
    }

    fun onPreviousWeek() {
        selectWeek(selectedWeekFlow.value.start.minusWeeks(1))
    }

    fun goToToday() {
        selectWeek(LocalDate.now())
    }

    fun selectWeek(targetDate: LocalDate) {
        val monday = targetDate.with(DayOfWeek.MONDAY)
        val sunday = targetDate.with(DayOfWeek.SUNDAY)
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
