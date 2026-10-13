package me.itzvirtual.myunitox.data.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.itzvirtual.myunitox.core.network.ApplicationScope
import me.itzvirtual.myunitox.data.config.local.AppConfig
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import org.koin.core.annotation.Single

@Single
class AppConfigRepository(
    private val configDao: AppConfigDao,
    @param:ApplicationScope private val externalScope: CoroutineScope,
) {
    val config: Flow<AppConfig?> = configDao.getConfig()

    // tokenFlow, tokenState and getTokenInMemory needed by NetworkModule
    val tokenFlow: Flow<String?> = config
        .map { it?.authToken }
        .distinctUntilChanged()

    val tokenState: StateFlow<String?> = tokenFlow
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun getTokenInMemory(): String? = tokenState.value

    suspend fun saveToken(token: String) = configDao.updateToken(token.trim())

    suspend fun getToken(): String? = configDao.getToken()

    fun getCalendarClientId(): String = "5852fd1ab7305612a8354d51"

    val selectedCalendarIdFlow: Flow<Int?> = configDao.getConfig()
        .map { config -> config?.selectedCalendarId }
        .distinctUntilChanged()

    suspend fun getSelectedCalendarId(): Int? = configDao.getSelectedCalendarId()

    suspend fun setSelectedCalendarId(calendarId: Int?) =
        configDao.updateSelectedCalendarId(calendarId)

    suspend fun clearAuth() = configDao.updateToken(null)
}
