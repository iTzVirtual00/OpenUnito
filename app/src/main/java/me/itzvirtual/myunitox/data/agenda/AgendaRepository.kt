package me.itzvirtual.myunitox.data.agenda

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.agenda.local.AgendaDao
import me.itzvirtual.myunitox.data.agenda.mapper.toDomain
import me.itzvirtual.myunitox.data.agenda.mapper.toEntity
import me.itzvirtual.myunitox.data.agenda.remote.PublicAgendaRequest
import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgendaRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: AgendaDao,
    private val json: Json,
) {
    fun buildKey(calendarLinkId: String, startDate: Instant, endDate: Instant) =
        "${calendarLinkId}_${startDate}_${endDate}"

    fun getAgendaFlow(calendarLinkId: String, startDate: Instant, endDate: Instant): Flow<List<AgendaEntry>?> =
        dao.getByKeyFlow(buildKey(calendarLinkId, startDate, endDate)).map { it?.toDomain(json) }

    fun isStaleFlow(calendarLinkId: String, startDate: Instant, endDate: Instant): Flow<Boolean> =
        dao.getByKeyFlow(buildKey(calendarLinkId, startDate, endDate)).map { entity ->
            entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.AGENDA_STALE_MS
        }

    /** Returns cached data if available, then fires a network refresh. Returns the fresh or cached list. */
    suspend fun getWithRefresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
    ): Pair<List<AgendaEntry>?, Result<List<AgendaEntry>>> {
        val key = buildKey(calendarLinkId, startDate, endDate)
        val cached = try {
            dao.getByKey(key)?.toDomain(json)
        } catch (_: Exception) {
            null
        }
        val refreshResult = refresh(calendarLinkId, clientId, startDate, endDate)
        val fresh = if (refreshResult.isSuccess) {
            try {
                dao.getByKey(key)?.toDomain(json)
            } catch (_: Exception) {
                null
            }
        } else null
        return cached to (fresh?.let { Result.success(it) } ?: refreshResult.map { emptyList() })
    }

    suspend fun getCached(calendarLinkId: String, startDate: Instant, endDate: Instant): List<AgendaEntry>? {
        dao.deleteOlderThan(System.currentTimeMillis() - CacheConfig.AGENDA_MAX_AGE_MS)
        return try {
            dao.getByKey(buildKey(calendarLinkId, startDate, endDate))?.toDomain(json)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun isCacheStale(calendarLinkId: String, startDate: Instant, endDate: Instant): Boolean {
        val entity = try {
            dao.getByKey(buildKey(calendarLinkId, startDate, endDate))
        } catch (_: Exception) {
            null
        }
        return entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.AGENDA_STALE_MS
    }

    suspend fun isCacheRecent(calendarLinkId: String, startDate: Instant, endDate: Instant): Boolean {
        val entity = try {
            dao.getByKey(buildKey(calendarLinkId, startDate, endDate))
        } catch (_: Exception) {
            null
        }
        return entity != null && System.currentTimeMillis() - entity.cachedAt < CacheConfig.AGENDA_RECENT_MS
    }

    suspend fun refresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
    ): Result<Unit> = try {
        val response = api.getPublicAgenda(
            PublicAgendaRequest(calendarLinkId, clientId, startDate.toString(), endDate.toString())
        )
        dao.upsert(response.toEntity(buildKey(calendarLinkId, startDate, endDate), json))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun validateCalendarLinkId(calendarLinkId: String, clientId: String): Boolean =
        try {
            val now = LocalDate.now()
            val start = now.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val end = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            api.getPublicAgenda(
                PublicAgendaRequest(calendarLinkId, clientId, start.toString(), end.toString())
            )
            true
        } catch (_: Exception) {
            false
        }

    suspend fun fetchTodayAndTomorrow(
        calendarLinkId: String,
        clientId: String,
    ): Result<List<AgendaEntry>> {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfTomorrow = today.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val cached = getCached(calendarLinkId, startOfToday, endOfTomorrow)
        if (cached != null) return Result.success(cached.sortedBy { it.startDateTime })
        return try {
            val response = api.getPublicAgenda(
                PublicAgendaRequest(
                    calendarLinkId,
                    clientId,
                    startOfToday.toString(),
                    endOfTomorrow.toString()
                )
            )
            dao.upsert(response.toEntity(buildKey(calendarLinkId, startOfToday, endOfTomorrow), json))
            Result.success(response.map { it.toDomain() }.sortedBy { it.startDateTime })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentOrNearestEntry(entries: List<AgendaEntry>): AgendaEntry? {
        val now = Instant.now()
        return entries.find { now >= it.startDateTime && now <= it.endDateTime }
            ?: entries.filter { it.startDateTime > now }.minByOrNull { it.startDateTime }
    }

    suspend fun clearCache() = dao.clearAll()
}
