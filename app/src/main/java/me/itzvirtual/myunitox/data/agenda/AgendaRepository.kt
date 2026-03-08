package me.itzvirtual.myunitox.data.agenda

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
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

private const val STALE_THRESHOLD_MS = 15 * 60 * 1000L

@Singleton
class AgendaRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: AgendaDao,
    private val json: Json,
) {
    fun buildKey(startDate: Instant, endDate: Instant) = "${startDate}_${endDate}"

    fun getAgendaFlow(startDate: Instant, endDate: Instant): Flow<List<AgendaEntry>?> =
        dao.getByKeyFlow(buildKey(startDate, endDate)).map { it?.toDomain(json) }

    fun isStaleFlow(startDate: Instant, endDate: Instant): Flow<Boolean> =
        dao.getByKeyFlow(buildKey(startDate, endDate)).map { entity ->
            entity == null || System.currentTimeMillis() - entity.cachedAt > STALE_THRESHOLD_MS
        }

    /** Returns cached data if available, then fires a network refresh. Returns the fresh or cached list. */
    suspend fun getWithRefresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
    ): Pair<List<AgendaEntry>?, Result<List<AgendaEntry>>> {
        val key = buildKey(startDate, endDate)
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

    suspend fun getCached(startDate: Instant, endDate: Instant): List<AgendaEntry>? =
        try {
            dao.getByKey(buildKey(startDate, endDate))?.toDomain(json)
        } catch (_: Exception) {
            null
        }

    suspend fun isCacheStale(startDate: Instant, endDate: Instant): Boolean {
        val entity = try {
            dao.getByKey(buildKey(startDate, endDate))
        } catch (_: Exception) {
            null
        }
        return entity == null || System.currentTimeMillis() - entity.cachedAt > STALE_THRESHOLD_MS
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
        dao.upsert(response.toEntity(buildKey(startDate, endDate), json))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchTodayAndTomorrow(
        calendarLinkId: String,
        clientId: String,
    ): Result<List<AgendaEntry>> {
        val today = LocalDate.now()
        val startOfToday = today.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endOfTomorrow = today.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val cached = getCached(startOfToday, endOfTomorrow)
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
            dao.upsert(response.toEntity(buildKey(startOfToday, endOfTomorrow), json))
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
