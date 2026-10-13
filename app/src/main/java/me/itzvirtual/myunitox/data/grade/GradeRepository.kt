package me.itzvirtual.myunitox.data.grade

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.ApplicationScope
import me.itzvirtual.myunitox.core.network.IoDispatcher
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.grade.local.ExamGradeDao
import me.itzvirtual.myunitox.data.grade.mapper.toDomain
import me.itzvirtual.myunitox.data.grade.mapper.toEntity
import me.itzvirtual.myunitox.domain.grade.ExamGrade
import org.koin.core.annotation.Single
import java.util.concurrent.atomic.AtomicReference

data class CachedGrades(
    val grades: List<ExamGrade>,
    val isStale: Boolean
)

@Single
class GradeRepository(
    private val api: UnitoApi,
    private val dao: ExamGradeDao,
    private val json: Json,

    @param:ApplicationScope private val externalScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val gradesFlow: Flow<CachedGrades?> = dao.get().map { entity ->
        if (entity == null) {
            triggerBackgroundRefresh()
            null
        } else {
            val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.GRADE_STALE_MS

            if (isStale) {
                triggerBackgroundRefresh()
            }

            val domainList = entity.toDomain(json)
                .sortedByDescending { it.score?.date }

            CachedGrades(grades = domainList, isStale = isStale)
        }
    }

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
            val response = api.getTranscript()
            dao.upsert(response.toEntity(json))
        }
    }
}
