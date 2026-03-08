package me.itzvirtual.myunitox.ui.feature_agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val agendaRepository: AgendaRepository,
    private val appConfigRepo: AppConfigRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow(AgendaScreenState())
    val screenState = _screenState.asStateFlow()

    init {
        val now = LocalDate.now()
        loadAgendaForWeek(
            startDate = now.with(java.time.DayOfWeek.MONDAY),
            endDate = now.with(java.time.DayOfWeek.SUNDAY),
        )
    }

    fun loadAgendaForWeek(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _screenState.update {
                it.copy(
                    selectedStart = startDate.with(java.time.DayOfWeek.MONDAY),
                    selectedEnd = endDate.with(java.time.DayOfWeek.SUNDAY),
                    isLoading = true,
                    agendaState = it.agendaState.copy(error = null),
                )
            }

            val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val calendarLinkId = appConfigRepo.getCalendarLinkId()
            val calendarClientId = appConfigRepo.getCalendarClientId()

            if (calendarLinkId == null) {
                _screenState.update {
                    it.copy(
                        isLoading = false,
                        agendaState = it.agendaState.copy(
                            error = "No calendar link ID. Set it in dev options.",
                        )
                    )
                }
                return@launch
            }

            // Cache-Then-Network: emit cached data immediately
            val cached = agendaRepository.getCached(startInstant, endInstant)
            val isStale = agendaRepository.isCacheStale(startInstant, endInstant)
            if (cached != null) {
                _screenState.update {
                    it.copy(
                        isLoading = false,
                        agendaState = it.agendaState.copy(
                            agendaEntries = cached,
                            isStale = isStale,
                        )
                    )
                }
            }

            // Refresh from network in background
            val result =
                agendaRepository.refresh(calendarLinkId, calendarClientId, startInstant, endInstant)
            result.fold(
                onSuccess = {
                    val fresh = agendaRepository.getCached(startInstant, endInstant)
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
                                error = if (cached == null) err.message else null,
                            )
                        )
                    }
                }
            )
        }
    }
}
