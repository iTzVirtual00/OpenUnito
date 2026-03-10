package me.itzvirtual.myunitox.data.grade

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.grade.local.ExamGradeDao
import me.itzvirtual.myunitox.data.grade.mapper.toDomain
import me.itzvirtual.myunitox.data.grade.mapper.toEntity
import me.itzvirtual.myunitox.domain.grade.ExamGrade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamGradeDao,
    private val json: Json,
) {
    val gradesFlow: Flow<List<ExamGrade>?> =
        dao.get().map { entity -> entity?.toDomain(json) }

    val isStaleFlow: Flow<Boolean> =
        dao.get().map { entity ->
            entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.GRADE_STALE_MS
        }

    suspend fun getCached(): List<ExamGrade>? {
        return try {
            val entity = dao.getOnce()
            if (entity != null && System.currentTimeMillis() - entity.cachedAt > CacheConfig.GRADE_STALE_MS) {
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
        return entity == null || System.currentTimeMillis() - entity.cachedAt > CacheConfig.GRADE_STALE_MS
    }

    suspend fun refresh(): Result<Unit> = try {
        val response = api.getTranscript()
        dao.upsert(response.toEntity(json))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
