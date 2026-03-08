package me.itzvirtual.myunitox.ui.feature_agenda

import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import java.time.LocalDate

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
)
