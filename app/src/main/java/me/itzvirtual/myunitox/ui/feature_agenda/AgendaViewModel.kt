package me.itzvirtual.myunitox.ui.feature_agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.calendar.CalendarConfigRepository
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfig
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class WeekRange(
    val start: LocalDate,
    val end: LocalDate
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    private val appConfigRepo: AppConfigRepository,
    private val calendarConfigRepo: CalendarConfigRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(AgendaScreenState())
    val screenState = _screenState.asStateFlow()

    // Internal state driving the active week
    private val currentWeek = run {
        val now = LocalDate.now()
        WeekRange(
            start = now.with(DayOfWeek.MONDAY),
            end = now.with(DayOfWeek.SUNDAY)
        )
    }
    private val selectedWeekFlow = MutableStateFlow(currentWeek)

    // Tracks ongoing refresh jobs to cancel prior network requests when changing weeks
    private var refreshJob: Job? = null

    init {
        observeCalendarsList()
        observeAgendaFlow()
    }

    /**
     * Reactively keeps available calendars and selected calendar updated in UI state.
     */
    private fun observeCalendarsList() {
        viewModelScope.launch {
            combine(
                calendarConfigRepo.calendarsFlow,
                calendarConfigRepo.selectedCalendarFlow
            ) { calendars, selected ->
                calendars to selected
            }.collectLatest { (calendars, selected) ->
                _screenState.update {
                    it.copy(
                        calendars = calendars,
                        selectedCalendar = selected
                    )
                }
            }
        }
    }

    /**
     * Core reactive engine:
     * Combines the selected calendar and the active week.
     * Whenever either changes, it switches observation to the matching Room entry.
     */
    private fun observeAgendaFlow() {
        viewModelScope.launch {
            combine(
                calendarConfigRepo.selectedCalendarFlow,
                selectedWeekFlow
            ) { selectedCalendar, week ->
                selectedCalendar to week
            }
                .distinctUntilChanged()
                .flatMapLatest { (calendar, week) ->
                    if (calendar == null) {
                        flowOf(Triple(null, week, null))
                    } else {
                        val startInstant =
                            week.start.atStartOfDay(ZoneId.systemDefault()).toInstant()
                        val endInstant =
                            week.end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

                        agendaRepository.getAgendaFlow(
                            calendar.calendarLinkId,
                            startInstant,
                            endInstant
                        )
                            .map { cachedAgenda ->
                                Triple(calendar, week, cachedAgenda)
                            }
                    }
                }
                .collectLatest { (calendar, week, cachedAgenda) ->
                    if (calendar == null) {
                        _screenState.update {
                            it.copy(
                                selectedStart = week.start,
                                selectedEnd = week.end,
                                isLoading = false,
                                agendaState = AgendaUiState(
                                    agendaEntries = emptyList(),
                                    error = "NO_CALENDAR"
                                )
                            )
                        }
                        return@collectLatest
                    }

                    val hasCachedData = cachedAgenda != null

                    // Update UI state in a single atomic update:
                    // - If data exists in Room, render it immediately with isLoading = false (ZERO flicker)
                    // - Only show isLoading = true if Room has no cache for this week
                    _screenState.update { current ->
                        current.copy(
                            selectedStart = week.start,
                            selectedEnd = week.end,
                            isLoading = !hasCachedData,
                            agendaState = current.agendaState.copy(
                                agendaEntries = cachedAgenda?.entries ?: emptyList(),
                                isStale = cachedAgenda?.isStale ?: true,
                                error = null
                            )
                        )
                    }

                    // Treat staleness purely as a background refresh trigger
                    if (!hasCachedData || cachedAgenda?.isStale == true) {
                        triggerBackgroundRefresh(
                            calendarLinkId = calendar.calendarLinkId,
                            week = week,
                            hasCachedData = hasCachedData
                        )
                    }
                }
        }
    }

    private fun triggerBackgroundRefresh(
        calendarLinkId: String,
        week: WeekRange,
        hasCachedData: Boolean
    ) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val startInstant = week.start.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endInstant = week.end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val clientId = appConfigRepo.getCalendarClientId()

            val result =
                agendaRepository.refresh(calendarLinkId, clientId, startInstant, endInstant)
            result.onFailure { err ->
                _screenState.update { current ->
                    current.copy(
                        isLoading = false,
                        agendaState = current.agendaState.copy(
                            error = if (err is IOException && hasCachedData) null else err.message
                        )
                    )
                }
            }
            // On success: Room updates its table, triggering dao.getByKeyFlow -> UI updates automatically!
        }
    }

    // --- Navigation ---

    fun onNextWeek() {
        val nextStart = selectedWeekFlow.value.start.plusWeeks(1)
        selectWeek(nextStart)
    }

    fun onPreviousWeek() {
        val prevStart = selectedWeekFlow.value.start.minusWeeks(1)
        selectWeek(prevStart)
    }

    fun goToToday() {
        val today = LocalDate.now()
        selectWeek(today)
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
}