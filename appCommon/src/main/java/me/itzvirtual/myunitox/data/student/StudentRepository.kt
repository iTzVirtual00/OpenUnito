package me.itzvirtual.myunitox.data.student

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
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.ApplicationScope
import me.itzvirtual.myunitox.core.network.IoDispatcher
import me.itzvirtual.myunitox.data.student.local.StudentDao
import me.itzvirtual.myunitox.data.student.mapper.toDomain
import me.itzvirtual.myunitox.data.student.mapper.toEntity
import me.itzvirtual.myunitox.data.student.remote.AppInitDto
import me.itzvirtual.myunitox.domain.student.StudentCareer
import me.itzvirtual.myunitox.domain.student.StudentData
import org.koin.core.annotation.Single
import java.util.concurrent.atomic.AtomicReference

data class CachedStudentData(
    val data: StudentData,
    val isStale: Boolean
)

@Single
class StudentRepository(
    private val client: HttpClient,
    private val dao: StudentDao,
    private val json: Json,

    @param:ApplicationScope private val externalScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val activeRefreshJob = AtomicReference<Job?>(null)
    private val refreshMutex = Mutex()

    val studentCacheFlow: Flow<CachedStudentData?> = dao.get()
        .map { entity ->
            if (entity == null) {
                triggerBackgroundRefresh()
                null
            } else {
                val isStale =
                    (System.currentTimeMillis() - entity.cachedAt) > CacheConfig.STUDENT_STALE_MS

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
        val currentJob = activeRefreshJob.get()
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
    suspend fun refresh(): Result<Unit> = runCatching {
        refreshMutex.withLock {
            withContext(ioDispatcher) {
                val appInit = client.get("app/init?os=android&os_version=16&app_version=25.7.0.0.17").body<AppInitDto>()
                val session = appInit.session ?: throw Exception("No auth session")

                dao.upsert(session.toEntity(json))
            }
        }
    }

    // This bullshit of a function exists because we need the career id to initialize UnitoApi
    // A design choice made in the first version that I KNEW would have made me suffer in the future ( now :_) )
    suspend fun getStudentData(): StudentData = withContext(ioDispatcher) {
        val cachedEntity = dao.getOneShot()
        if (cachedEntity != null) {
            runCatching { cachedEntity.toDomain(json) }.getOrNull()?.let { return@withContext it }
        }

        refresh().getOrThrow()

        val freshEntity = dao.getOneShot()
            ?: throw IllegalStateException("Student data missing after successful refresh")

        freshEntity.toDomain(json)
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
