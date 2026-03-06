package me.itzvirtual.myunitox.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import me.itzvirtual.myunitox.data.local.dao.AppConfigDao
import me.itzvirtual.myunitox.data.models.AppConfig
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AppConfigRepository @Inject constructor(
    private val configDao: AppConfigDao
) {
    // Expose as a Flow so the UI can react to changes automatically
    val config: Flow<AppConfig?> = configDao.getConfig()

    suspend fun saveToken(token: String) {
        configDao.updateToken(token.trim())
    }

    suspend fun getToken(): String? = configDao.getToken()

    /** Synchronous — only call from a background thread (e.g. OkHttp interceptor). */
    fun getTokenBlocking(): String? = configDao.getTokenBlocking()

    suspend fun getCalendarLinkId(): String? = configDao.getCalendarLinkId()

    suspend fun getCalendarClientId(): String =
        configDao.getCalendarClientId() ?: "5852fd1ab7305612a8354d51"

    suspend fun setCalendarLinkId(linkId: String) {
        configDao.updateCalendarLink(linkId.trim())
    }

    suspend fun clearAuth() {
        configDao.updateToken(null)
    }
}