package me.itzvirtual.myunitox.data.examresult

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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

data class CachedExamResults(
    val results: List<ExamResult>?,
    val isStale: Boolean
)

@Singleton
class ExamResultRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamResultDao,
    private val json: Json,
) {
    val examResultsCacheFlow: Flow<CachedExamResults> = dao.get()
        .map { entity ->
            if (entity == null) {
                CachedExamResults(results = null, isStale = true)
            } else {
                val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.EXAM_RESULT_STALE_MS
                val domainList = entity.toDomain(json)?.sortedByDescending { it.date }
                CachedExamResults(results = domainList, isStale = isStale)
            }
        }
        .distinctUntilChanged()

    val examResultsFlow: Flow<List<ExamResult>?> = examResultsCacheFlow
        .map { it.results }
        .distinctUntilChanged()

    suspend fun refresh(): Result<Unit> = runCatching {
        val response = api.getExamBoard()
        dao.upsert(response.toEntity(json))
    }

    suspend fun getExamResults(): List<ExamResult> {
        val currentCached = examResultsFlow.first()
        if (currentCached != null) return currentCached

        refresh().getOrThrow()
        return examResultsFlow.filterNotNull().first()
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}