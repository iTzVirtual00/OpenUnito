package me.itzvirtual.myunitox.data.agenda

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.ApplicationScope
import me.itzvirtual.myunitox.core.network.IoDispatcher
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.agenda.local.AgendaDao
import me.itzvirtual.myunitox.data.agenda.mapper.toDomain
import me.itzvirtual.myunitox.data.agenda.mapper.toEntity
import me.itzvirtual.myunitox.data.agenda.remote.PublicAgendaRequest
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
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

    private val appConfigRepo: AppConfigRepository,

    @param:ApplicationScope private val externalScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    // refreshes deduplication
    private val activeRefreshes = ConcurrentHashMap<String, Job>()
    private val refreshMutex = Mutex()


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
                    backgroundRefresh(calendarLinkId,  appConfigRepo.getCalendarClientId(), startDate, endDate)
                    null
                } else {
                    val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.AGENDA_STALE_MS

                    if (isStale) {
                        backgroundRefresh(calendarLinkId,  appConfigRepo.getCalendarClientId(), startDate, endDate)
                    }

                    val domainList = runCatching {
                        entity.toDomain(json).sortedBy { entry -> entry.startDateTime }
                    }.getOrDefault(emptyList())

                    CachedAgenda(entries = domainList, isStale = isStale)
                }
            }
            .distinctUntilChanged()
            .flowOn(ioDispatcher)
    }

    private fun backgroundRefresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant
    ) {
        val key = buildKey(calendarLinkId, startDate, endDate)

        activeRefreshes.compute(key) { _, currentJob ->
            if (currentJob?.isActive == true) {
                currentJob // Already refreshing
            } else {
                externalScope.launch(ioDispatcher) {
                    try {
                        refresh(calendarLinkId, clientId, startDate, endDate)
                    } catch (_: Exception) {
                        // TODO: notify something
                    } finally {
                        activeRefreshes.remove(key)
                    }
                }
            }
        }
    }

    // Prevents concurrent network calls and ensures an out-of-order,
    // slower response never overwrites newer Room data.
    // TODO: needed? (also used in all the other repos)
    suspend fun refresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
    ): Result<Unit> = runCatching {
        refreshMutex.withLock {
            val response = api.getPublicAgenda(
                PublicAgendaRequest(calendarLinkId, clientId, startDate.toString(), endDate.toString())
            )
            val key = buildKey(calendarLinkId, startDate, endDate)
            dao.upsert(response.toEntity(key, json))
        }
    }

    suspend fun validateCalendarLinkId(calendarLinkId: String, clientId: String): Boolean {
        return try {
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
    }

    suspend fun clearCache() = dao.clearAll()
}