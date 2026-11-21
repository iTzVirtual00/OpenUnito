package me.itzvirtual.openunito.data.student

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.itzvirtual.openunito.core.cache.CacheConfig
import me.itzvirtual.openunito.core.network.CareerContext
import me.itzvirtual.openunito.core.network.UnitoApi
import me.itzvirtual.openunito.data.DataUtils
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.student.local.StudentDao
import me.itzvirtual.openunito.data.student.local.StudentEntity
import me.itzvirtual.openunito.data.student.mapper.toDomain
import me.itzvirtual.openunito.data.student.mapper.toEntity
import me.itzvirtual.openunito.data.student.remote.AppInitDto
import me.itzvirtual.openunito.data.student.remote.AppInitError
import me.itzvirtual.openunito.domain.getNowMillis
import me.itzvirtual.openunito.domain.student.StudentData
import org.koin.compose.koinInject
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

data class CachedStudentData(
    val data: StudentData,
    val isStale: Boolean
)

@OptIn(ExperimentalAtomicApi::class, ExperimentalCoroutinesApi::class)
@Single
class StudentRepository(
    private val client: HttpClient,
    private val dao: StudentDao,
    private val json: Json,
    private val appConfigRepo: AppConfigRepository,

    private val dataUtils: DataUtils,

    @Named("ApplicationScope") private val externalScope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
): KoinComponent {

    // deferred injection to prevent circular dep
    // : KoinComponent is needed to call inject
    private val api: UnitoApi by inject()

    private val activeRefreshJob = AtomicReference<Job?>(null)

    // React to appConfigRepo.tokenState (on login)
    val studentCacheFlow: StateFlow<CachedStudentData?> = appConfigRepo.tokenState
        .flatMapLatest { token ->
            if (token.isNullOrBlank()) {
                flowOf(null)
            } else {
                dao.observe().map { entity ->
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
            }
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val careerContextFlow: SharedFlow<CareerContext> = combine(
        studentCacheFlow.filterNotNull().map { it.data },
        appConfigRepo.selectedCareerIdState
    ) { studentData, selectedCareer ->
        val studentCareers = studentData.careers

        val currentCareer = selectedCareer.let { id ->
            studentCareers.firstOrNull { it.id == id }
        }

        return@combine currentCareer ?: run {
            val firstCareer = studentCareers.firstOrNull()
                ?: throw IllegalStateException("No careers found for student")
            Logger.w(tag = "StudentRepository") { "Selected career was invalid, fallbacking to first" }
            appConfigRepo.setCareerId(firstCareer.id)
            firstCareer
        }
    }
        .map { CareerContext(it.id, it.registrationId) }
        .distinctUntilChanged()
        // shareIn make it so that early subscribers don't get a null value
        .shareIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            replay = 1
        )


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

    suspend fun refresh(): Result<StudentEntity> = runCatching {
        withContext(ioDispatcher) {
            // TODO: Do something with an unsuccessful response
            val resp = client.get("app/init?os=android&os_version=16&app_version=25.7.0.0.17")
            if(resp.status.value != 200) throw AppInitError.HttpError(resp.status)

            val appInit = resp.body<AppInitDto>()
            val session = appInit.session ?: throw AppInitError.Generic("Session is null")

            val entity = session.toEntity(json)
            dao.upsert(entity)
            entity
        }
    }.onFailure { err ->
        Logger.e(tag = "StudentRepository") {
            "Init failed with `${err.message}`; deauthenticating and purging caches"
        }
        appConfigRepo.clearAuth()
        dataUtils.purgeAllCaches()
    }

    suspend fun logout() {
        api.logout()
        appConfigRepo.clearAuth()
    }

    suspend fun clearCache() {
        withContext(ioDispatcher) {
            dao.clearAll()
        }
    }
}
