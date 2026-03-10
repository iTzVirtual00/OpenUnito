package me.itzvirtual.myunitox.data.calendar

import kotlinx.coroutines.flow.Flow
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfig
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfigDao
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarConfigRepository @Inject constructor(
    private val dao: CalendarConfigDao,
    private val appConfigRepo: AppConfigRepository,
) {
    val calendarsFlow: Flow<List<CalendarConfig>> = dao.getAllFlow()

    suspend fun getAll(): List<CalendarConfig> = dao.getAll()

    suspend fun getById(id: Int): CalendarConfig? = dao.getById(id)

    suspend fun add(name: String, calendarLinkId: String): CalendarConfig {
        val config = CalendarConfig(name = name, calendarLinkId = calendarLinkId)
        val id = dao.upsert(config)
        return config.copy(id = id.toInt())
    }

    suspend fun rename(id: Int, newName: String) {
        val existing = dao.getById(id) ?: return
        dao.upsert(existing.copy(name = newName))
    }

    suspend fun delete(id: Int) {
        dao.deleteById(id)
        // If the deleted calendar was selected, clear the selection
        if (appConfigRepo.getSelectedCalendarId() == id) {
            appConfigRepo.setSelectedCalendarId(null)
        }
    }

    suspend fun getSelectedCalendar(): CalendarConfig? {
        val selectedId = appConfigRepo.getSelectedCalendarId()
        if (selectedId != null) {
            val cal = dao.getById(selectedId)
            if (cal != null) return cal
        }
        // Fall back to first available calendar
        return dao.getAll().firstOrNull()
    }

    suspend fun setSelectedCalendarId(id: Int) {
        appConfigRepo.setSelectedCalendarId(id)
    }
}
