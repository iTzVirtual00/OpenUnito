package me.itzvirtual.myunitox.data.agenda

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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

data class CachedAgenda(
    val entries: List<AgendaEntry>,
    val isStale: Boolean
)

@Singleton
class AgendaRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: AgendaDao,
    private val json: Json,
) {
    fun buildKey(calendarLinkId: String, startDate: Instant, endDate: Instant): String =
        "${calendarLinkId}_${startDate}_${endDate}"

    fun getAgendaFlow(
        calendarLinkId: String,
        startDate: Instant,
        endDate: Instant
    ): Flow<CachedAgenda?> {
        val key = buildKey(calendarLinkId, startDate, endDate)
        return dao.getByKeyFlow(key)
            .map { entity ->
                if (entity == null) {
                    null
                } else {
                    val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.AGENDA_STALE_MS
                    val domainList = runCatching {
                        entity.toDomain(json).sortedBy { entry -> entry.startDateTime }
                    }.getOrDefault(emptyList())

                    CachedAgenda(entries = domainList, isStale = isStale)
                }
            }
            .distinctUntilChanged()
    }

    suspend fun refresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
    ): Result<Unit> = runCatching {
        val response = api.getPublicAgenda(
            PublicAgendaRequest(calendarLinkId, clientId, startDate.toString(), endDate.toString())
        )
        val key = buildKey(calendarLinkId, startDate, endDate)
        dao.upsert(response.toEntity(key, json))
    }

    suspend fun getCached(
        calendarLinkId: String,
        startDate: Instant,
        endDate: Instant
    ): CachedAgenda? {
        dao.deleteOlderThan(System.currentTimeMillis() - CacheConfig.AGENDA_MAX_AGE_MS)
        val entity = runCatching {
            dao.getByKey(buildKey(calendarLinkId, startDate, endDate))
        }.getOrNull() ?: return null

        val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.AGENDA_STALE_MS
        val domainList = runCatching {
            entity.toDomain(json).sortedBy { it.startDateTime }
        }.getOrDefault(emptyList())

        return CachedAgenda(entries = domainList, isStale = isStale)
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
        if (cached != null && !cached.isStale) {
            return Result.success(cached.entries)
        }

        val refreshResult = refresh(calendarLinkId, clientId, startOfToday, endOfTomorrow)
        return refreshResult.mapCatching {
            getCached(calendarLinkId, startOfToday, endOfTomorrow)?.entries ?: cached?.entries ?: emptyList()
        }
    }

    suspend fun clearCache() = dao.clearAll()
}