package me.itzvirtual.myunitox.data.config

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.itzvirtual.myunitox.data.config.local.AppConfig
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepository @Inject constructor(
    private val configDao: AppConfigDao,
) {
    val config: Flow<AppConfig?> = configDao.getConfig()

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
