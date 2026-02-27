package me.itzvirtual.myunitox.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.local.SharedPrefStorage
import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.repository.AgendaRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AgendaUiState(
    val agendaEntries: List<AgendaEntry> = emptyList(),
    val selectedStart: LocalDate? = null,
    val selectedEnd: LocalDate? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    private val sharedPrefStorage: SharedPrefStorage
) : ViewModel() {
    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val now = LocalDate.now()

        loadAgendaForWeek(
            startDate = now.with(java.time.DayOfWeek.MONDAY),
            endDate = now.with(java.time.DayOfWeek.SUNDAY)
        )
    }

    fun loadAgendaForWeek(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedStart = startDate.with(java.time.DayOfWeek.MONDAY),
                    selectedEnd = endDate.with(java.time.DayOfWeek.SUNDAY),
                    isLoading = true, errorMessage = null
                )
            }
            val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val calendarLinkId = sharedPrefStorage.getCalendarLinkId()
            val calendarClientId = sharedPrefStorage.getCalendarClientId()
            if (calendarLinkId == null) {
                _uiState.update { state ->
                    state.copy(errorMessage = "No calendar link ID found. Please set it in dev options.", isLoading = false)
                }
                return@launch
            }
            val result = agendaRepository.fetchPublicAgenda(
                calendarLinkId = calendarLinkId,
                clientId = calendarClientId,
                startDate = startInstant,
                endDate = endInstant
            )
            // Yet another kotlin feature! (why so many ways to do THE SAME, F*, THING)
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(agendaEntries = data, isLoading = false)
                    }
                },
                onFailure = { err ->
                    _uiState.update { state ->
                        state.copy(errorMessage = err.message, isLoading = false)
                    }
                }
            )
        }
    }
}