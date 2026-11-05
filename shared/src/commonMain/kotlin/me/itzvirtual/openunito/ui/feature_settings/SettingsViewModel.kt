package me.itzvirtual.openunito.ui.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.agenda.local.AgendaDao
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.dashboard.local.DashboardDao
import me.itzvirtual.openunito.data.examdate.local.ExamDateDao
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.grade.local.ExamGradeDao
import me.itzvirtual.openunito.data.localization.SupportedLanguage
import me.itzvirtual.openunito.data.localization.SupportedRegion
import me.itzvirtual.openunito.data.student.local.StudentDao
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel (
    private val appConfigRepo: AppConfigRepository,
    private val agendaDao: AgendaDao,
    private val dashboardDao: DashboardDao,
    private val studentDao: StudentDao,
    private val examResultDao: ExamResultDao,
    private val examDateDao: ExamDateDao,
    private val examGradeDao: ExamGradeDao,
): ViewModel() {
    val screenState: StateFlow<SettingsScreenState> = appConfigRepo.showDevState
        .map{ showDev, ->
            SettingsScreenState(
                showDev = showDev,
            )
        }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsScreenState()
    )

    fun setShowDev(value: Boolean) {
        viewModelScope.launch {
            appConfigRepo.setShowDev(value)
        }
    }

    fun setLanguage(language: SupportedLanguage) {
        viewModelScope.launch {
            appConfigRepo.setLanguage(language)
        }

    }

    fun setRegion(region: SupportedRegion) {
        viewModelScope.launch {
            appConfigRepo.setRegion(region)
        }
    }

    fun clearToken(onComplete: () -> Unit = {}) {
        viewModelScope.launch { appConfigRepo.clearAuth(); onComplete() }
    }

    fun purgeAllCaches(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            agendaDao.clearAll()
            dashboardDao.clearAll()
            studentDao.clearAll()
            examResultDao.clearAll()
            examDateDao.clearAll()
            examGradeDao.clearAll()
            onComplete()
        }
    }
}