package me.itzvirtual.openunito.data.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.itzvirtual.openunito.data.config.local.AppConfig
import me.itzvirtual.openunito.data.config.local.AppConfigDao
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AppConfigRepository(
    private val configDao: AppConfigDao,
    @Named("ApplicationScope") private val externalScope: CoroutineScope,
) {
    // TODO: use StateFlow?
    val config: Flow<AppConfig?> = configDao.getConfig()


    val tokenState: StateFlow<String?> = config
        .map { it?.authToken }
        .distinctUntilChanged() // TODO: Mario check this, isn't it be redundant?
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun getToken(): String? = tokenState.value
    suspend fun saveToken(token: String) = configDao.updateToken(token.trim())
    suspend fun clearAuth() = configDao.updateToken(null)


    val selectedCareerIdState: StateFlow<Int?> = config
        .map { it?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    suspend fun setCareerId(newId: Int) {
        configDao.updateCareerId(newId)
    }


    val selectedCalendarIdState: StateFlow<Int?> = config
        .map { config -> config?.selectedCalendarId }
        .distinctUntilChanged()
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun getSelectedCalendarId(): Int? = selectedCalendarIdState.value
    fun getCalendarClientId(): String = "5852fd1ab7305612a8354d51"
    suspend fun setSelectedCalendarId(calendarId: Int?) =
        configDao.updateSelectedCalendarId(calendarId)

    // Debug //
    val debug: Flow<Boolean> = configDao.getDebug()

    suspend fun setDebug(value: Boolean) = configDao.updateDebug(value)
}
