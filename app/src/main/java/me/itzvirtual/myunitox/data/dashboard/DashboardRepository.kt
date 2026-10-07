package me.itzvirtual.myunitox.data.dashboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.GradeStatusSerializer
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.dashboard.local.DashboardDao
import me.itzvirtual.myunitox.data.dashboard.mapper.toDomain
import me.itzvirtual.myunitox.data.dashboard.mapper.toEntity
import me.itzvirtual.myunitox.data.examresult.mapper.toDomain
import me.itzvirtual.myunitox.domain.dashboard.DashboardData
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import me.itzvirtual.myunitox.domain.examresult.GradeStatus
import javax.inject.Inject
import javax.inject.Singleton

data class CachedDashboard(
    val data: DashboardData?,
    val isStale: Boolean
)

@Singleton
class DashboardRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: DashboardDao,
    private val json: Json,
) {
    val dashboardCacheFlow: Flow<CachedDashboard> = dao.get()
        .map { entity ->
            if (entity == null) {
                CachedDashboard(data = null, isStale = true)
            } else {
                val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.DASHBOARD_STALE_MS
                CachedDashboard(data = entity.toDomain(json), isStale = isStale)
            }
        }
        .distinctUntilChanged()

    val dashboardDataFlow: Flow<DashboardData?> = dashboardCacheFlow
        .map { it.data }
        .distinctUntilChanged()

    suspend fun refresh(): Result<Unit> = runCatching {
        val response = api.getDashboardData()
        dao.upsert(response.toEntity(json))
    }

    // one shot
    suspend fun getDashboardData(): DashboardData {
        val currentCached = dashboardDataFlow.first()
        if (currentCached != null) return currentCached

        refresh().getOrThrow()
        return dashboardDataFlow.filterNotNull().first()
    }

    // actions
    suspend fun changeGradeStatus(gradeId: String, gradeStatus: GradeStatus): Result<ExamResult> = runCatching {
        val result = api.changeGradeStatus(gradeId, GradeStatusSerializer.serializeToString(gradeStatus))
        // Re-sync local dashboard state if a grade acceptance modifies overall dashboard counts/stats
        refresh()
        result.toDomain()
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}