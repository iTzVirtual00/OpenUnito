package me.itzvirtual.openunito.data.grade

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.core.network.UnitoApi
import me.itzvirtual.openunito.data.grade.local.ExamGradeDao
import me.itzvirtual.openunito.data.grade.mapper.toDomain
import me.itzvirtual.openunito.data.grade.mapper.toEntity
import me.itzvirtual.openunito.data.student.StudentRepository
import me.itzvirtual.openunito.domain.getNowMillis
import me.itzvirtual.openunito.domain.grade.ExamGrade
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class CachedGrades(
    val grades: List<ExamGrade>,
    val isStale: Boolean
)

@OptIn(ExperimentalAtomicApi::class)
@Single
class GradeRepository(
    private val api: UnitoApi,
    private val dao: ExamGradeDao,
    private val json: Json,

    private val studentRepo: StudentRepository,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val gradesFlow: Flow<CachedGrades?> = studentRepo.careerContextFlow
        .distinctUntilChanged()
        .flatMapLatest { career ->
            dao.observe(career.id).map { entity ->
                if (entity == null) {
                    triggerBackgroundRefresh(career.id)
                    null
                } else {
                    val isStale = getNowMillis() - entity.cachedAt > CacheConfig.GRADE_STALE_MS

                    if (isStale) {
                        triggerBackgroundRefresh(career.id)
                    }

                    val domainList = entity.toDomain(json)
                        .sortedByDescending { it.score?.date }

                    CachedGrades(grades = domainList, isStale = isStale)
                }
            }
                // Initial value (So loading state emitted immediately on career switch)
                .onStart { emit(null) }
        }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    private fun triggerBackgroundRefresh(careerId: Int) {
        val currentJob = activeRefreshJob.load()
        if (currentJob?.isActive == true) return

        val newJob = externalScope.launch(ioDispatcher) {
            try {
                refresh(careerId)
            } catch (_: Exception) {
                // TODO: notify something
            } finally {
                activeRefreshJob.compareAndSet(coroutineContext[Job], null)
            }
        }

        if (!activeRefreshJob.compareAndSet(currentJob, newJob)) {
            newJob.cancel()
        }
    }

    suspend fun refresh(careerId: Int): Result<Unit> = runCatching {
        refreshMutex.withLock {
            val response = api.getTranscript()
            dao.upsert(response.toEntity(json, careerId))
        }
    }
}
