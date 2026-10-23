package me.itzvirtual.openunito.data.calendar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.itzvirtual.openunito.data.agenda.AgendaRepository
import me.itzvirtual.openunito.data.calendar.local.CalendarConfig
import me.itzvirtual.openunito.data.calendar.local.CalendarConfigDao
import me.itzvirtual.openunito.data.config.AppConfigRepository
import org.koin.core.annotation.Single

@Single
class CalendarConfigRepository(
    private val dao: CalendarConfigDao,
    private val appConfigRepo: AppConfigRepository,
    private val agendaRepo: AgendaRepository,
) {
    val calendarsFlow: Flow<List<CalendarConfig>> = dao.getAllFlow()

    fun getByIdFlow(id: Int): Flow<CalendarConfig?> = calendarsFlow
        .map { list -> list.find { it.id == id } }
        .distinctUntilChanged()

    val selectedCalendarFlow: Flow<CalendarConfig?> = combine(
        appConfigRepo.selectedCalendarIdState,
        calendarsFlow
    ) { selectedId, calendars ->
        if (selectedId != null) {
            calendars.find { it.id == selectedId } ?: calendars.firstOrNull()
        } else {
            calendars.firstOrNull()
        }
    }.distinctUntilChanged()

    suspend fun getAll(): List<CalendarConfig> = dao.getAll()

    suspend fun getById(id: Int): CalendarConfig? = dao.getById(id)

    suspend fun getSelectedCalendar(): CalendarConfig? {
        val selected = appConfigRepo.getSelectedCalendarId() ?: return null
        return dao.getById(selected)
    }

    suspend fun add(name: String, calendarLinkId: String): CalendarConfig {
        val config = CalendarConfig(name = name, calendarLinkId = calendarLinkId)
        val id = dao.upsert(config)
        return config.copy(id = id.toInt())
    }

    suspend fun edit(id: Int, newName: String, newLink: String): CalendarConfig {
        val existing = dao.getById(id) ?: CalendarConfig(name = newName, calendarLinkId = newLink)
        val newConf = existing.copy(name = newName, calendarLinkId = newLink)
        dao.upsert(newConf)
        return newConf
    }

    suspend fun delete(id: Int) {
        dao.deleteById(id)
        if (appConfigRepo.getSelectedCalendarId() == id) {
            appConfigRepo.setSelectedCalendarId(dao.getAll().firstOrNull()?.id)
        }
    }

    suspend fun setSelectedCalendarId(id: Int?) {
        appConfigRepo.setSelectedCalendarId(id)
    }
}
