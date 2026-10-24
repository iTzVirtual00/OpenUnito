package me.itzvirtual.openunito.data.examresult

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.core.network.UnitoApi
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.examresult.mapper.toDomain
import me.itzvirtual.openunito.data.examresult.mapper.toEntity
import me.itzvirtual.openunito.data.student.StudentRepository
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

    private val studentRepo: StudentRepository,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val examResultsCacheFlow: Flow<CachedExamResults?> = studentRepo.careerContextFlow
        .distinctUntilChanged()
        .flatMapLatest { career ->
            dao.observe(career.id)
                .map { entity ->
                    if (entity == null) {
                        triggerBackgroundRefresh(career.id)
                        null
                    } else {
                        val isStale =
                            (getNowMillis() - entity.cachedAt) > CacheConfig.EXAM_RESULT_STALE_MS

                        if (isStale) {
                            triggerBackgroundRefresh(career.id)
                        }

                        val domainList = entity.toDomain(json).sortedByDescending { it.date }
                        CachedExamResults(results = domainList, isStale = isStale)
                    }
                }
                // Initial value (So loading state emitted immediately on career switch)
                .onStart { emit(null) }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    private fun triggerBackgroundRefresh(careerId: Int) {
        val currentJob = activeRefreshJob.load()
        if (currentJob?.isActive == true) return

        val newJob = externalScope.launch(ioDispatcher) {
            try {
                refresh(careerId)
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

    suspend fun refresh(careerId: Int): Result<Unit> = runCatching {
        refreshMutex.withLock {
            val response = api.getExamBoard()
            dao.upsert(response.toEntity(json, careerId))
        }
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}
