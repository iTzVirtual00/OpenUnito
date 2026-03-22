package me.itzvirtual.myunitox.data.examresult

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.examresult.local.ExamResultDao
import me.itzvirtual.myunitox.data.examresult.mapper.toDomain
import me.itzvirtual.myunitox.data.examresult.mapper.toEntity
import me.itzvirtual.myunitox.domain.examresult.ExamResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamResultRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamResultDao,
    private val json: Json,
) {
    val examResultsFlow: Flow<List<ExamResult>?> =
        dao.get().map { entity -> entity?.toDomain(json) }

    val isStaleFlow: Flow<Boolean> =
        dao.get().map { entity ->
            entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.EXAM_RESULT_STALE_MS
        }

    suspend fun getCached(): List<ExamResult>? {
        return try {
            val entity = dao.getOnce()
            if (entity != null && System.currentTimeMillis() - entity.cachedAt > CacheConfig.EXAM_RESULT_STALE_MS) {
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
        return entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.EXAM_RESULT_STALE_MS
    }

    suspend fun refresh(): Result<Unit> = try {
        val response = api.getExamBoard()
        dao.upsert(response.toEntity(json))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
