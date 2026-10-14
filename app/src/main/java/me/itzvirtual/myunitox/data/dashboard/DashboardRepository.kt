package me.itzvirtual.myunitox.data.dashboard

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
import me.itzvirtual.myunitox.core.network.GradeStatusSerializer
import me.itzvirtual.myunitox.core.network.IoDispatcher
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.dashboard.local.DashboardDao
import me.itzvirtual.myunitox.data.dashboard.mapper.toDomain
import me.itzvirtual.myunitox.data.dashboard.mapper.toEntity
import me.itzvirtual.myunitox.data.examresult.mapper.toDomain
import me.itzvirtual.myunitox.domain.dashboard.DashboardData
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import org.koin.core.annotation.Single
import java.util.concurrent.atomic.AtomicReference

data class CachedDashboard(
    val data: DashboardData?,
    val isStale: Boolean
)

@Single
class DashboardRepository(
    private val api: UnitoApi,
    private val dao: DashboardDao,
    private val json: Json,

    @param:ApplicationScope private val externalScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val dashboardCacheFlow: Flow<CachedDashboard?> = dao.get()
        .map { entity ->
            if (entity == null) {
                triggerBackgroundRefresh()
                null
            } else {
                val isStale =
                    (System.currentTimeMillis() - entity.cachedAt) > CacheConfig.DASHBOARD_STALE_MS

                if (isStale) {
                    triggerBackgroundRefresh()
                }

                val domainData = runCatching { entity.toDomain(json) }.getOrNull()
                CachedDashboard(data = domainData, isStale = isStale)
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

        // last race condition check to see if another job was registered in the meantime
        // compareAndSet sets its value to newJob is the current is still currentJob
        // returns false if that is not true
        if (!activeRefreshJob.compareAndSet(currentJob, newJob)) {
            // Another thread set a job in the meantime; cancel this one
            newJob.cancel()
        }
    }

    suspend fun refresh(): Result<Unit> = runCatching {
        refreshMutex.withLock {
            val response = api.getDashboardData()
            dao.upsert(response.toEntity(json))
        }
    }

    // Actions
    suspend fun changeGradeStatus(gradeId: String, gradeStatus: GradeStatus): Result<ExamResult> =
        runCatching {
            val result =
                api.changeGradeStatus(gradeId, GradeStatusSerializer.serializeToString(gradeStatus))
//        refresh()
            result.toDomain()
        }

    suspend fun clearCache() {
        dao.clearAll()
    }
}
