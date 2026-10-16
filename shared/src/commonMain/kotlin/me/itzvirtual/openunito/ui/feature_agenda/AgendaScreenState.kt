package me.itzvirtual.openunito.ui.feature_agenda

import kotlinx.datetime.LocalDate
import me.itzvirtual.openunito.data.calendar.local.CalendarConfig
import me.itzvirtual.openunito.domain.agenda.AgendaEntry

data class AgendaUiState(
    val agendaEntries: List<AgendaEntry> = emptyList(),
    val isStale: Boolean = false,
    val error: String? = null,
)

data class AgendaScreenState(
    val agendaState: AgendaUiState = AgendaUiState(),
    val selectedStart: LocalDate? = null,
    val selectedEnd: LocalDate? = null,
    val isLoading: Boolean = true,
    val calendars: List<CalendarConfig> = emptyList(),
    val selectedCalendar: CalendarConfig? = null,
)
