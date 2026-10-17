package me.itzvirtual.openunito.data.examresult

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
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.examresult.mapper.toDomain
import me.itzvirtual.openunito.data.examresult.mapper.toEntity
import me.itzvirtual.openunito.domain.examresult.ExamResult
import me.itzvirtual.openunito.domain.getNowMillis
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class CachedExamResults(
    val results: List<ExamResult>?,
    val isStale: Boolean
)

@OptIn(ExperimentalAtomicApi::class)
@Single
class ExamResultRepository(
    private val api: UnitoApi,
    private val dao: ExamResultDao,
    private val json: Json,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val examResultsCacheFlow: Flow<CachedExamResults?> = dao.get()
        .map { entity ->
            if (entity == null) {
                triggerBackgroundRefresh()
                null
            } else {
                val isStale =
                    (getNowMillis() - entity.cachedAt) > CacheConfig.EXAM_RESULT_STALE_MS

                if (isStale) {
                    triggerBackgroundRefresh()
                }

                val domainList = entity.toDomain(json).sortedByDescending { it.date }
                CachedExamResults(results = domainList, isStale = isStale)
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
            val response = api.getExamBoard()
            dao.upsert(response.toEntity(json))
        }
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}
