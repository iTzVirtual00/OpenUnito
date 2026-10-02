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

data class CachedGrades(
    val grades: List<ExamGrade>,
    val isStale: Boolean
)
@Singleton
class GradeRepository @Inject constructor(
    private val api: UnitoApi,
    private val dao: ExamGradeDao,
    private val json: Json,
) {

    val gradesFlow: Flow<CachedGrades?> = dao.get().map { entity ->
        if (entity == null) null
        else {
            val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.GRADE_STALE_MS
            val domainList = entity.toDomain(json)
                .sortedByDescending { it.score?.date }

            CachedGrades(grades = domainList, isStale = isStale)
        }
    }

    suspend fun refresh(): Result<Unit> = runCatching {
        val response = api.getTranscript()
        dao.upsert(response.toEntity(json))
    }
}
