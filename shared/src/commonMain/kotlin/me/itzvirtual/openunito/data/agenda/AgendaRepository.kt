package me.itzvirtual.openunito.data.agenda

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.core.network.UnitoApi
import me.itzvirtual.openunito.data.agenda.local.AgendaDao
import me.itzvirtual.openunito.data.agenda.mapper.toDomain
import me.itzvirtual.openunito.data.agenda.mapper.toEntity
import me.itzvirtual.openunito.data.agenda.remote.PublicAgendaRequest
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.domain.agenda.AgendaEntry
import me.itzvirtual.openunito.domain.getCurrentTimeZone
import me.itzvirtual.openunito.domain.getNowMillis
import me.itzvirtual.openunito.domain.getToday
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Instant

data class CachedAgenda(
    val entries: List<AgendaEntry>,
    val isStale: Boolean
)

@Single
class AgendaRepository(
    private val api: UnitoApi,
    private val dao: AgendaDao,
    private val json: Json,

    private val appConfigRepo: AppConfigRepository,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    // refreshes deduplication
    private val activeRefreshes = mutableMapOf<String, Job>()
    private val jobsMutex = Mutex()

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
                    triggerBackgroundRefresh(
                        calendarLinkId,
                        appConfigRepo.getCalendarClientId(),
                        startDate,
                        endDate
                    )
                    null
                } else {
                    val isStale =
                        getNowMillis() - entity.cachedAt > CacheConfig.AGENDA_STALE_MS

                    if (isStale) {
                        triggerBackgroundRefresh(
                            calendarLinkId,
                            appConfigRepo.getCalendarClientId(),
                            startDate,
                            endDate
                        )
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

    private fun triggerBackgroundRefresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant
    ) {
        val key = buildKey(calendarLinkId, startDate, endDate)

        externalScope.launch(ioDispatcher) {
            jobsMutex.withLock {
                val shouldStart = let {
                    val currentJob = activeRefreshes[key]
                    currentJob?.isActive != true
                }
                if (!shouldStart) return@launch

                val job = launch {
                    try {
                        refresh(calendarLinkId, clientId, startDate, endDate)
                    } catch (_: Exception) {
                        // TODO: notify something
                    } finally {
                        jobsMutex.withLock {
                            if (activeRefreshes[key] === coroutineContext[Job]) activeRefreshes.remove(
                                key
                            )
                        }
                    }
                }
                activeRefreshes[key] = job
            }
        }
    }

    private suspend fun refresh(
        calendarLinkId: String,
        clientId: String,
        startDate: Instant,
        endDate: Instant,
    ): Result<Unit> = runCatching {
        val response = api.getPublicAgenda(
            PublicAgendaRequest(
                calendarLinkId,
                clientId,
                startDate.toString(),
                endDate.toString()
            )
        )
        val key = buildKey(calendarLinkId, startDate, endDate)
        dao.upsert(response.toEntity(key, json))
    }

    suspend fun validateCalendarLinkId(calendarLinkId: String, clientId: String): Boolean {
        return try {
            val today = getToday()
            val tomorrow = today.plus(1, DateTimeUnit.DAY)

            val start = today.atStartOfDayIn(getCurrentTimeZone())
            val end = tomorrow.atStartOfDayIn(getCurrentTimeZone())
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
