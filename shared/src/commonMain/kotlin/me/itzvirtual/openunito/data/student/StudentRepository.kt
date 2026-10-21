package me.itzvirtual.openunito.data.student

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.data.student.local.StudentDao
import me.itzvirtual.openunito.data.student.local.StudentEntity
import me.itzvirtual.openunito.data.student.mapper.toDomain
import me.itzvirtual.openunito.data.student.mapper.toEntity
import me.itzvirtual.openunito.data.student.remote.AppInitDto
import me.itzvirtual.openunito.domain.getNowMillis
import me.itzvirtual.openunito.domain.student.StudentCareer
import me.itzvirtual.openunito.domain.student.StudentData
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class CachedStudentData(
    val data: StudentData,
    val isStale: Boolean
)

@OptIn(ExperimentalAtomicApi::class)
@Single
class StudentRepository(
    private val client: HttpClient,
    private val dao: StudentDao,
    private val json: Json,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val studentDataMutex = Mutex()

    val studentCacheFlow: Flow<CachedStudentData?> = dao.get()
        .map { entity ->
            if (entity == null) {
                triggerBackgroundRefresh()
                null
            } else {
                val isStale =
                    (getNowMillis() - entity.cachedAt) > CacheConfig.STUDENT_STALE_MS

                if (isStale) {
                    triggerBackgroundRefresh()
                }

                val domainData = runCatching { entity.toDomain(json) }.getOrNull()
                if (domainData == null) {
                    null
                } else {
                    CachedStudentData(data = domainData, isStale = isStale)
                }
            }
        }
        .distinctUntilChanged()
        .flowOn(ioDispatcher)

    val studentDataFlow: Flow<StudentData?> = studentCacheFlow
        .map { it?.data }
        .distinctUntilChanged()

    private fun triggerBackgroundRefresh() {
        val currentJob = activeRefreshJob.load()
        if (currentJob?.isActive == true) return

        val newJob = externalScope.launch(ioDispatcher) {
            try {
                refresh()
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

    // TODO: fix multiple requests to /init with cold cache
    suspend fun refresh(): Result<StudentEntity> = runCatching {
        withContext(ioDispatcher) {
            val appInit = client.get("app/init?os=android&os_version=16&app_version=25.7.0.0.17").body<AppInitDto>()
            val session = appInit.session ?: throw Exception("No auth session")

            val entity = session.toEntity(json)
            dao.upsert(entity)
            entity
        }
    }

    // This bullshit of a function exists because we need the career id to initialize UnitoApi
    // A design choice made in the first version that I KNEW would have made me suffer in the future ( now :_) )
    suspend fun getStudentData(): StudentData = withContext(ioDispatcher) {
        val entity = dao.getOneShot() ?: studentDataMutex.withLock {
            dao.getOneShot() ?: refresh().getOrThrow()
        }

        runCatching { entity.toDomain(json) }
            .getOrElse { e ->
                throw IllegalStateException("Failed to parse student data domain model", e)
            }
    }

    suspend fun getCurrentCareer(): StudentCareer =
        getStudentData().careers.firstOrNull {
            it.id != 0/*REDACTED*/ // TODO: tf is this + add career selection
        }
            ?: throw IllegalStateException("No careers found for student")

    suspend fun checkAuth(): Boolean = refresh().isSuccess

    suspend fun clearCache() {
        withContext(ioDispatcher) {
            dao.clearAll()
        }
    }
}
