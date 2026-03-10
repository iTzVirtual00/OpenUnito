package me.itzvirtual.myunitox.ui.feature_agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.calendar.CalendarConfigRepository
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfig
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    private val appConfigRepo: AppConfigRepository,
    private val calendarConfigRepo: CalendarConfigRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(AgendaScreenState())
    val screenState = _screenState.asStateFlow()

    private var currentLoadJob: Job? = null

    init {
        viewModelScope.launch {
            // Load calendars and selected calendar
            val calendars = calendarConfigRepo.getAll()
            val selected = calendarConfigRepo.getSelectedCalendar()
            _screenState.update {
                it.copy(
                    calendars = calendars,
                    selectedCalendar = selected,
                )
            }
            val now = LocalDate.now()
            loadAgendaForWeek(
                startDate = now.with(java.time.DayOfWeek.MONDAY),
                endDate = now.with(java.time.DayOfWeek.SUNDAY),
            )
        }
    }

    fun selectCalendar(calendar: CalendarConfig) {
        viewModelScope.launch {
            calendarConfigRepo.setSelectedCalendarId(calendar.id)
            _screenState.update { it.copy(selectedCalendar = calendar) }
            val state = _screenState.value
            val start = state.selectedStart ?: LocalDate.now().with(java.time.DayOfWeek.MONDAY)
            val end = state.selectedEnd ?: start.with(java.time.DayOfWeek.SUNDAY)
            loadAgendaForWeek(start, end)
        }
    }

    fun addCalendar(name: String, calendarLinkId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val clientId = appConfigRepo.getCalendarClientId()
            val isValid = agendaRepository.validateCalendarLinkId(calendarLinkId, clientId)
            if (!isValid) {
                onResult(false, "Invalid calendar ID — could not fetch data.")
                return@launch
            }
            val newCal = calendarConfigRepo.add(name, calendarLinkId)
            val calendars = calendarConfigRepo.getAll()
            calendarConfigRepo.setSelectedCalendarId(newCal.id)
            _screenState.update {
                it.copy(calendars = calendars, selectedCalendar = newCal)
            }
            onResult(true, "")
            // Load data for the newly added calendar
            val state = _screenState.value
            val start = state.selectedStart ?: LocalDate.now().with(java.time.DayOfWeek.MONDAY)
            val end = state.selectedEnd ?: start.with(java.time.DayOfWeek.SUNDAY)
            loadAgendaForWeek(start, end)
        }
    }

    fun renameCalendar(id: Int, newName: String) {
        viewModelScope.launch {
            calendarConfigRepo.rename(id, newName)
            val calendars = calendarConfigRepo.getAll()
            val selected = _screenState.value.selectedCalendar
            _screenState.update {
                it.copy(
                    calendars = calendars,
                    selectedCalendar = if (selected?.id == id) selected.copy(name = newName) else selected,
                )
            }
        }
    }

    fun deleteCalendar(id: Int) {
        viewModelScope.launch {
            calendarConfigRepo.delete(id)
            val calendars = calendarConfigRepo.getAll()
            val selected = calendarConfigRepo.getSelectedCalendar()
            _screenState.update {
                it.copy(calendars = calendars, selectedCalendar = selected)
            }
            if (selected != null) {
                val state = _screenState.value
                val start = state.selectedStart ?: LocalDate.now().with(java.time.DayOfWeek.MONDAY)
                val end = state.selectedEnd ?: start.with(java.time.DayOfWeek.SUNDAY)
                loadAgendaForWeek(start, end)
            } else {
                _screenState.update {
                    it.copy(
                        isLoading = false,
                        agendaState = AgendaUiState(error = "NO_CALENDAR"),
                    )
                }
            }
        }
    }

    fun loadAgendaForWeek(startDate: LocalDate, endDate: LocalDate) {
        currentLoadJob?.cancel()
        currentLoadJob = viewModelScope.launch {
            val adjustedStart = startDate.with(java.time.DayOfWeek.MONDAY)
            val adjustedEnd = endDate.with(java.time.DayOfWeek.SUNDAY)
            val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val selectedCalendar = _screenState.value.selectedCalendar
            if (selectedCalendar == null) {
                _screenState.update {
                    it.copy(
                        selectedStart = adjustedStart,
                        selectedEnd = adjustedEnd,
                        isLoading = false,
                        agendaState = AgendaUiState(error = "NO_CALENDAR"),
                    )
                }
                return@launch
            }

            val calendarLinkId = selectedCalendar.calendarLinkId

            // Read cache BEFORE setting loading state — makes week scrolling instant
            val cached = agendaRepository.getCached(calendarLinkId, startInstant, endInstant)
            val isStale = agendaRepository.isCacheStale(calendarLinkId, startInstant, endInstant)

            _screenState.update {
                it.copy(
                    selectedStart = adjustedStart,
                    selectedEnd = adjustedEnd,
                    isLoading = cached == null,
                    agendaState = if (cached != null) {
                        it.agendaState.copy(
                            agendaEntries = cached,
                            isStale = isStale,
                            error = null,
                        )
                    } else {
                        it.agendaState.copy(agendaEntries = emptyList(), error = null)
                    },
                )
            }

            // Skip network refresh if cache is recent (< 5 min)
            if (agendaRepository.isCacheRecent(calendarLinkId, startInstant, endInstant)) {
                _screenState.update { it.copy(isLoading = false) }
                return@launch
            }

            val calendarClientId = appConfigRepo.getCalendarClientId()

            val result =
                agendaRepository.refresh(calendarLinkId, calendarClientId, startInstant, endInstant)
            result.fold(
                onSuccess = {
                    val fresh = agendaRepository.getCached(calendarLinkId, startInstant, endInstant)
                    if (fresh != null) {
                        _screenState.update {
                            it.copy(
                                isLoading = false,
                                agendaState = it.agendaState.copy(
                                    agendaEntries = fresh,
                                    isStale = false,
                                    error = null,
                                )
                            )
                        }
                    }
                },
                onFailure = { err ->
                    _screenState.update {
                        it.copy(
                            isLoading = cached == null,
                            agendaState = it.agendaState.copy(
                                error = if (err is IOException && cached != null) null else err.message,
                            )
                        )
                    }
                }
            )
        }
    }
}
