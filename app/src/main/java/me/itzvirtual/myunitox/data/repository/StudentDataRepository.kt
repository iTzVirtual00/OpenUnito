package me.itzvirtual.myunitox.data.repository

import kotlinx.coroutines.sync.Mutex
import me.itzvirtual.myunitox.data.models.AppInit
import me.itzvirtual.myunitox.data.models.StudentCareer
import me.itzvirtual.myunitox.data.models.StudentData
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.itzvirtual.myunitox.data.remote.API_URL
import javax.inject.Inject
import javax.inject.Singleton

// special repository because:
// responsible for fetching and caching student initialization data.
// It does not use Retrofit because UnitoAPI.kt (provideUnitoAPI) depends on /app/init (this)
@Singleton
class StudentDataRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    private val mutex = Mutex()

    private var studentData: StudentData? = null
    private var error = false

    // Expose a read-only flow of student data, cached
    // not sure if this a correct pattern
    // TODO: make this return a Result and emit errors, as the other repositories do
    suspend fun getStudentData(): StudentData {
        studentData?.let { return it }

        return mutex.withLock {
            // Double-check, did someone else fetch it while we waited?
            studentData ?: fetchAppInit()!!.also {
                studentData = it
            }
        }
    }

    suspend fun checkAuth(): Boolean {
        return try {
            val fetched = fetchAppInit()
            if(studentData == null && fetched != null) {
                mutex.withLock {
                    studentData = fetched
                }
            }
            fetched != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentCareer(): StudentCareer {
        return getStudentData().careers.firstOrNull()
            ?: throw IllegalStateException("No careers found for student")
    }

    private suspend fun fetchAppInit(): StudentData? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${API_URL}app/init?os=android&os_version=16&app_version=25.7.0.0.17")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("AppInit failed with code: ${response.code}")
            }
            val body = response.body.string()
            val appInit = json.decodeFromString<AppInit>(body)
            appInit.session
        }
    }
}


