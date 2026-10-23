package me.itzvirtual.openunito.data.dashboard

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.core.network.GradeStatusSerializer
import me.itzvirtual.openunito.core.network.UnitoApi
import me.itzvirtual.openunito.data.dashboard.local.DashboardDao
import me.itzvirtual.openunito.data.dashboard.mapper.toDomain
import me.itzvirtual.openunito.data.dashboard.mapper.toEntity
import me.itzvirtual.openunito.data.examresult.mapper.toDomain
import me.itzvirtual.openunito.data.student.StudentRepository
import me.itzvirtual.openunito.domain.dashboard.DashboardData
import me.itzvirtual.openunito.domain.examresult.ExamResult
import me.itzvirtual.openunito.domain.examresult.GradeStatus
import me.itzvirtual.openunito.domain.getNowMillis
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

data class CachedDashboard(
    val data: DashboardData?,
    val isStale: Boolean
)

@Single
class DashboardRepository(
    private val api: UnitoApi,
    private val dao: DashboardDao,
    private val json: Json,

    private val studentRepo: StudentRepository,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = atomic<Job?>(null)
    private val refreshMutex = Mutex()

    // null == loading
    val dashboardCacheFlow: Flow<CachedDashboard?> = studentRepo.careerContextFlow
        .distinctUntilChanged()
        .flatMapLatest { career ->
            dao.observe(career.id)
                .map { entity ->
                    if (entity == null) {
                        triggerBackgroundRefresh(career.id)
                        null
                    } else {
                        val isStale =
                            (getNowMillis() - entity.cachedAt) > CacheConfig.DASHBOARD_STALE_MS
                        if (isStale) {
                            triggerBackgroundRefresh(career.id)
                        }

                        val domainData = runCatching { entity.toDomain(json) }.getOrNull()
                        CachedDashboard(data = domainData, isStale = isStale)
                    }
                }
                // Initial value (So loading state emitted immediately on career switch)
                .onStart { emit(null) }
        }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    private fun triggerBackgroundRefresh(careerId: Int) {
        val currentJob = activeRefreshJob.value
        if (currentJob?.isActive == true) return

        val newJob = externalScope.launch(ioDispatcher, start = CoroutineStart.LAZY) {
            try {
                refresh(careerId).onFailure {
                    // TODO: notify something
                }
            } finally {
                activeRefreshJob.compareAndSet(coroutineContext[Job], null)
            }
        }

        // last race condition check to see if another job was registered in the meantime
        // compareAndSet sets its value to newJob is the current is still currentJob
        // returns false if that is not true
        if (activeRefreshJob.compareAndSet(currentJob, newJob)) {
            newJob.start()
        } else {
            // Another thread set a job in the meantime; cancel this one
            newJob.cancel()
        }
    }

    suspend fun refresh(careerId: Int): Result<Unit> = runCatching {
        refreshMutex.withLock {
            val response = api.getDashboardData()
            dao.upsert(response.toEntity(json, careerId))
        }
    }

    // Actions
    suspend fun changeGradeStatus(gradeId: String, gradeStatus: GradeStatus): Result<ExamResult> =
        runCatching {
            val result =
                api.changeGradeStatus(gradeId, GradeStatusSerializer.serializeToString(gradeStatus))
            triggerBackgroundRefresh(studentRepo.careerContextFlow.first().id)
            result.toDomain()
        }
}
