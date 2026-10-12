package me.itzvirtual.myunitox.data.examresult

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
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.ApplicationScope
import me.itzvirtual.myunitox.core.network.IoDispatcher
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.examresult.local.ExamResultDao
import me.itzvirtual.myunitox.data.examresult.mapper.toDomain
import me.itzvirtual.myunitox.data.examresult.mapper.toEntity
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

data class CachedExamResults(
    val results: List<ExamResult>?,
    val isStale: Boolean
)

@Singleton
class ExamResultRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamResultDao,
    private val json: Json,

    @param:ApplicationScope private val externalScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
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
                    (System.currentTimeMillis() - entity.cachedAt) > CacheConfig.EXAM_RESULT_STALE_MS

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
        val currentJob = activeRefreshJob.get()
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