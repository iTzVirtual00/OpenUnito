package me.itzvirtual.myunitox.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.PublicAgendaRequest
import me.itzvirtual.myunitox.data.remote.UnitoAPI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache key for agenda data based on date range
 */
private data class AgendaCacheKey(
    val startDate: Instant,
    val endDate: Instant
)

/**
 * Repository for agenda data with caching support.
 * Acts as single source of truth - data is fetched once and shared across ViewModels.
 */
@Singleton
class AgendaRepository @Inject constructor(private val api: UnitoAPI) {

    private val mutex = Mutex()
    private val cache = mutableMapOf<AgendaCacheKey, List<AgendaEntry>>()

    // StateFlow for tomorrow's agenda - shared across ViewModels
    private val _tomorrowAgenda = MutableStateFlow<List<AgendaEntry>>(emptyList())
    val tomorrowAgenda: StateFlow<List<AgendaEntry>> = _tomorrowAgenda.asStateFlow()

    private var tomorrowLoaded = false

    suspend fun fetchPublicAgenda(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
        forceRefresh: Boolean = false
    ): Result<List<AgendaEntry>> {
        val cacheKey = AgendaCacheKey(startDate, endDate)

        // Return cached data if available and not forcing refresh
        if (!forceRefresh) {
            cache[cacheKey]?.let { return Result.success(it) }
        }

        return try {
            val response = api.getPublicAgenda(
                PublicAgendaRequest(
                    calendarLinkId,
                    clientId,
                    startDate.toString(),
                    endDate.toString()
                )
            )
            cache[cacheKey] = response
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches today's and tomorrow's agenda combined.
     * Returns entries sorted by start time.
     */
    suspend fun fetchTodayAndTomorrowAgenda(
        calendarLinkId: String,
        clientId: String,
        forceRefresh: Boolean = false
    ): Result<List<AgendaEntry>> {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfTomorrow = today.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()

        return fetchPublicAgenda(
            calendarLinkId = calendarLinkId,
            clientId = clientId,
            startDate = startOfToday,
            endDate = endOfTomorrow,
            forceRefresh = forceRefresh
        ).map { entries ->
            entries.sortedBy { it.startDateTime }
        }
    }

    /**
     * Gets the current in-progress event, or the nearest upcoming event.
     */
    fun getCurrentOrNearestEntry(entries: List<AgendaEntry>): AgendaEntry? {
        val now = Instant.now()

        // First, check for in-progress event
        val inProgress = entries.find { entry ->
            now >= entry.startDateTime && now <= entry.endDateTime
        }
        if (inProgress != null) return inProgress

        // Otherwise, find the nearest upcoming event
        return entries
            .filter { it.startDateTime > now }
            .minByOrNull { it.startDateTime }
    }

    fun clearCache() {
        cache.clear()
        _tomorrowAgenda.value = emptyList()
        tomorrowLoaded = false
    }
}