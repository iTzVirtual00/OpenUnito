package me.itzvirtual.myunitox.data.examdate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.UnitoApi
import me.itzvirtual.myunitox.data.examdate.local.ExamDateDao
import me.itzvirtual.myunitox.data.examdate.mapper.toDomain
import me.itzvirtual.myunitox.data.examdate.mapper.toEntity
import me.itzvirtual.myunitox.domain.examdate.ExamDate
import javax.inject.Inject
import javax.inject.Singleton

data class CachedExamDates(
    val dates: List<ExamDate>?,
    val isStale: Boolean
)

@Singleton
class ExamDateRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamDateDao,
    private val json: Json,
) {
    /**
     * Emits cached exam dates along with staleness whenever Room updates.
     */
    val examDatesCacheFlow: Flow<CachedExamDates> = dao.get()
        .map { entity ->
            if (entity == null) {
                CachedExamDates(dates = null, isStale = true)
            } else {
                val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.EXAM_DATE_STALE_MS
                CachedExamDates(dates = entity.toDomain(json), isStale = isStale)
            }
        }
        .distinctUntilChanged()

    /**
     * Direct domain model flow for consumers that only need the latest list.
     */
    val examDatesFlow: Flow<List<ExamDate>?> = examDatesCacheFlow
        .map { it.dates }
        .distinctUntilChanged()

    /**
     * Refreshes the exam dates from the network and updates the Room cache.
     */
    suspend fun refresh(): Result<Unit> = runCatching {
        val response = api.getExamDates()
        dao.upsert(response.toEntity(json))
    }

    /**
     * One-shot read: returns cached data immediately if available,
     * otherwise triggers a network refresh and awaits the saved entity.
     */
    suspend fun getExamDates(): List<ExamDate> {
        val currentCached = examDatesFlow.first()
        if (currentCached != null) return currentCached

        refresh().getOrThrow()
        return examDatesFlow.filterNotNull().first()
    }

    suspend fun clearCache() {
        dao.clearAll()
    }
}