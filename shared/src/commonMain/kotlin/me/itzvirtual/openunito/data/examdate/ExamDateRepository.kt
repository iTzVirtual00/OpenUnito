package me.itzvirtual.openunito.data.examdate

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
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.core.network.UnitoApi
import me.itzvirtual.openunito.data.examdate.local.ExamDateDao
import me.itzvirtual.openunito.data.examdate.mapper.toDomain
import me.itzvirtual.openunito.data.examdate.mapper.toEntity
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.getNowMillis
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class CachedExamDates(
    val dates: List<ExamDate>,
    val isStale: Boolean
)

@OptIn(ExperimentalAtomicApi::class)
@Single
class ExamDateRepository(
    private val api: UnitoApi,
    private val dao: ExamDateDao,
    private val json: Json,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val examDatesCacheFlow: Flow<CachedExamDates?> = dao.get()
        .map { entity ->
            if (entity == null) {
                triggerBackgroundRefresh()
                null
            } else {
                val isStale =
                    (getNowMillis() - entity.cachedAt) > CacheConfig.EXAM_DATE_STALE_MS

                if (isStale) {
                    triggerBackgroundRefresh()
                }

                val domainList = runCatching { entity.toDomain(json) }.getOrNull()
                if (domainList == null) {
                    null
                } else {
                    CachedExamDates(dates = domainList, isStale = isStale)
                }
            }
        }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    private fun triggerBackgroundRefresh() {
        val currentJob = activeRefreshJob.load()
        if (currentJob?.isActive == true) return

        val newJob = externalScope.launch(ioDispatcher) {
            try {
                refresh()
            } catch (_: Exception) {
                // TODO: notify something
            } finally {
                activeRefreshJob.compareAndSet(coroutineContext[Job], null)
            }
        }

        if (!activeRefreshJob.compareAndSet(currentJob, newJob)) {
            newJob.cancel()
        }
    }

    suspend fun refresh(): Result<Unit> = runCatching {
        refreshMutex.withLock {
            val response = api.getExamDates()
            dao.upsert(response.toEntity(json))
        }
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}
