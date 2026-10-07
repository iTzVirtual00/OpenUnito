package me.itzvirtual.myunitox.data.student

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.cache.CacheConfig
import me.itzvirtual.myunitox.core.network.API_URL
import me.itzvirtual.myunitox.data.student.local.StudentDao
import me.itzvirtual.myunitox.data.student.mapper.toDomain
import me.itzvirtual.myunitox.data.student.mapper.toEntity
import me.itzvirtual.myunitox.data.student.remote.AppInitDto
import me.itzvirtual.myunitox.domain.student.StudentCareer
import me.itzvirtual.myunitox.domain.student.StudentData
import okhttp3.OkHttpClient
import okhttp3.Request

data class CachedStudentData(
    val data: StudentData?,
    val isStale: Boolean
)

class StudentRepository(
    private val client: OkHttpClient,
    private val json: Json,
) {
    private val daoState = MutableStateFlow<StudentDao?>(null)

    // Setter called when database module gets the value
    var studentDao: StudentDao?
        get() = daoState.value
        set(value) {
            daoState.value = value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val studentCacheFlow: Flow<CachedStudentData> = daoState
        .flatMapLatest { dao ->
            dao?.get() ?: flowOf(null)
        }
        .map { entity ->
            if (entity == null) {
                CachedStudentData(data = null, isStale = true)
            } else {
                val isStale = System.currentTimeMillis() - entity.cachedAt > CacheConfig.STUDENT_STALE_MS
                CachedStudentData(data = entity.toDomain(json), isStale = isStale)
            }
        }
        .distinctUntilChanged()

    val studentDataFlow: Flow<StudentData?> = studentCacheFlow
        .map { it.data }
        .distinctUntilChanged()

    suspend fun getStudentData(): StudentData {
        val currentCached = studentDataFlow.first()
        if (currentCached != null) return currentCached

        refresh().getOrThrow()
        return studentDataFlow.filterNotNull().first()
    }

    suspend fun getCurrentCareer(): StudentCareer =
        getStudentData().careers.firstOrNull()
            ?: throw IllegalStateException("No careers found for student")

    suspend fun checkAuth(): Boolean = refresh().isSuccess

    suspend fun refresh(): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${API_URL}app/init?os=android&os_version=16&app_version=25.7.0.0.17")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("AppInit failed: ${response.code}")
                val body = response.body.string()
                val appInit = json.decodeFromString<AppInitDto>(body)
                val session = appInit.session ?: throw Exception("No auth session")

                daoState.value?.upsert(session.toEntity(json))
            }
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            daoState.value?.clearAll()
        }
    }
}