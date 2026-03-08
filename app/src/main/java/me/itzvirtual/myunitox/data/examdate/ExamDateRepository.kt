package me.itzvirtual.myunitox.data.examdate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.examdate.local.ExamDateDao
import me.itzvirtual.myunitox.data.examdate.mapper.toDomain
import me.itzvirtual.myunitox.data.examdate.mapper.toEntity
import me.itzvirtual.myunitox.domain.examdate.ExamDate
import javax.inject.Inject
import javax.inject.Singleton

private const val STALE_THRESHOLD_MS = 30 * 60 * 1000L

@Singleton
class ExamDateRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamDateDao,
    private val json: Json,
) {
    val examDatesFlow: Flow<List<ExamDate>?> =
        dao.get().map { entity -> entity?.toDomain(json) }

    val isStaleFlow: Flow<Boolean> =
        dao.get().map { entity ->
            entity == null || System.currentTimeMillis() - entity.cachedAt > STALE_THRESHOLD_MS
        }

    suspend fun getCached(): List<ExamDate>? =
        try {
            dao.getOnce()?.toDomain(json)
        } catch (_: Exception) {
            null
        }

    suspend fun isCacheStale(): Boolean {
        val entity = try {
            dao.getOnce()
        } catch (_: Exception) {
            null
        }
        return entity == null || System.currentTimeMillis() - entity.cachedAt > STALE_THRESHOLD_MS
    }

    suspend fun refresh(): Result<Unit> = try {
        val response = api.getExamDates()
        dao.upsert(response.toEntity(json))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
