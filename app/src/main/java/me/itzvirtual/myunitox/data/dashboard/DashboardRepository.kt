package me.itzvirtual.myunitox.data.dashboard

import kotlinx.coroutines.flow.Flow
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

@Singleton
class DashboardRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: DashboardDao,
    private val json: Json,
) {
    val dashboardDataFlow: Flow<DashboardData?> =
        dao.get().map { entity -> entity?.toDomain(json) }

    val isStaleFlow: Flow<Boolean> =
        dao.get().map { entity ->
            entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.DASHBOARD_STALE_MS
        }

    suspend fun refresh(): Result<Unit> = try {
        val response = api.getDashboardData()
        dao.upsert(response.toEntity(json))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCached(): DashboardData? {
        return try {
            val entity = dao.getOnce()
            if (entity != null && System.currentTimeMillis() - entity.cachedAt > CacheConfig.DASHBOARD_STALE_MS) {
                dao.clearAll()
                return null
            }
            entity?.toDomain(json)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun isCacheStale(): Boolean {
        val entity = try {
            dao.getOnce()
        } catch (_: Exception) {
            null
        }
        return entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.DASHBOARD_STALE_MS
    }

    suspend fun changeGradeStatus(gradeId: String, gradeStatus: GradeStatus): Result<ExamResult> =
        try {
            val result =
                api.changeGradeStatus(gradeId, GradeStatusSerializer.serializeToString(gradeStatus))
            Result.success(result.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
}
