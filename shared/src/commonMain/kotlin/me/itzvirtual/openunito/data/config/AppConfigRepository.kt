package me.itzvirtual.openunito.data.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.itzvirtual.openunito.data.config.local.AppConfig
import me.itzvirtual.openunito.data.config.local.AppConfigDao
import me.itzvirtual.openunito.data.localization.SupportedLanguage
import me.itzvirtual.openunito.data.localization.SupportedRegion
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class AppConfigRepository(
    private val configDao: AppConfigDao,
    @Named("ApplicationScope") private val externalScope: CoroutineScope,
) {
    val config: StateFlow<AppConfig?> = configDao.getConfig()
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )


    val tokenState: StateFlow<String?> = config
        .map { it?.authToken }
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
    val debugState: StateFlow<Boolean?> = config
        .map {
            if (it == null) null
            else {
                it.debug && it.showDev
            }
        }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    suspend fun setDebug(value: Boolean) = configDao.updateDebug(value)

    // ShowAttendance //
    val showAttendanceState: StateFlow<Boolean?> = config
        .map { it?.showAttendance }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    suspend fun setShowAttendance(value: Boolean) = configDao.updateShowAttendance(value)

    // ShowDev //
    val showDevState: StateFlow<Boolean?> = config
        .map { it?.showDev }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    suspend fun setShowDev(value: Boolean) = configDao.updateShowDev(value)

    // selected language //
    val selectedLocaleState: StateFlow<Pair<String, String>?> = config
        .map { (it?.selectedLanguage ?: "") to (it?.selectedRegion ?: "") }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    suspend fun setLanguage(language: SupportedLanguage){
        configDao.updateSelectedLanguage(language.tag)
    }

    suspend fun setRegion(language: SupportedRegion){
        configDao.updateSelectedRegion(language.regionCode)
    }
}
