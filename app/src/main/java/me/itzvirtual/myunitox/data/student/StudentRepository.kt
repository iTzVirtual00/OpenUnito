package me.itzvirtual.myunitox.data.student

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.itzvirtual.myunitox.core.network.API_URL
import me.itzvirtual.myunitox.data.student.local.StudentDao
import me.itzvirtual.myunitox.data.student.mapper.toDomain
import me.itzvirtual.myunitox.data.student.mapper.toEntity
import me.itzvirtual.myunitox.data.student.remote.AppInitDto
import me.itzvirtual.myunitox.domain.student.StudentCareer
import me.itzvirtual.myunitox.domain.student.StudentData
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Constructed explicitly in NetworkModule so that UnitoApi can depend on it.
 * The StudentDao is injected via [setDao] after the database is ready.
 * This breaks the circular dependency: NetworkModule → StudentRepository ← DatabaseModule.
 */
class StudentRepository(
    private val client: OkHttpClient,
    private val json: Json,
) {
    // Injected lazily after DI graph is assembled
    internal var studentDao: StudentDao? = null

    private val mutex = Mutex()
    private var inMemoryCache: StudentData? = null

    val studentDataFlow: Flow<StudentData?>
        get() = studentDao?.get()?.map { it?.toDomain(json) }
            ?: kotlinx.coroutines.flow.flowOf(null)

    suspend fun getStudentData(): StudentData {
        inMemoryCache?.let { return it }
        return mutex.withLock {
            inMemoryCache ?: run {
                // Try Room cache first
                val cached = try {
                    studentDao?.get()?.first()?.toDomain(json)
                } catch (_: Exception) {
                    null
                }

                if (cached != null) {
                    inMemoryCache = cached
                    cached
                } else {
                    fetchAndCache()
                }
            }
        }
    }

    suspend fun checkAuth(): Boolean = try {
        fetchAndCache()
        true
    } catch (_: Exception) {
        false
    }

    suspend fun getCurrentCareer(): StudentCareer =
        getStudentData().careers.firstOrNull()
            ?: throw IllegalStateException("No careers found for student")

    suspend fun refresh(): Result<Unit> = try {
        fetchAndCache()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun fetchAndCache(): StudentData = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${API_URL}app/init?os=android&os_version=16&app_version=25.7.0.0.17")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("AppInit failed: ${response.code}")
            val body = response.body.string()
            val appInit = json.decodeFromString<AppInitDto>(body)
            val session = appInit.session ?: throw Exception("No auth session")
            studentDao?.upsert(session.toEntity(json))
            val domain = session.toDomain()
            inMemoryCache = domain
            domain
        }
    }

    fun clearCache() {
        inMemoryCache = null
    }
}
