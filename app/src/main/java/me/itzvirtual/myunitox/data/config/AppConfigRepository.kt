package me.itzvirtual.myunitox.data.config

import kotlinx.coroutines.flow.Flow
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

    fun getTokenBlocking(): String? = configDao.getTokenBlocking()

    suspend fun getCalendarLinkId(): String? = configDao.getCalendarLinkId()

    suspend fun getCalendarClientId(): String =
        configDao.getCalendarClientId() ?: "5852fd1ab7305612a8354d51"

    suspend fun setCalendarLinkId(linkId: String) = configDao.updateCalendarLink(linkId.trim())

    suspend fun clearAuth() = configDao.updateToken(null)
}
