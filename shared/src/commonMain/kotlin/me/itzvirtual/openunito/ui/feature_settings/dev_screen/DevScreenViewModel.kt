package me.itzvirtual.openunito.ui.feature_settings.dev_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.agenda.local.AgendaDao
import me.itzvirtual.openunito.data.config.AppConfigRepository
import me.itzvirtual.openunito.data.dashboard.local.DashboardDao
import me.itzvirtual.openunito.data.examdate.local.ExamDateDao
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.grade.local.ExamGradeDao
import me.itzvirtual.openunito.data.student.local.StudentDao
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DevScreenViewModel (
    private val appConfigRepo: AppConfigRepository,
    private val agendaDao: AgendaDao,
    private val dashboardDao: DashboardDao,
    private val studentDao: StudentDao,
    private val examResultDao: ExamResultDao,
    private val examDateDao: ExamDateDao,
    private val examGradeDao: ExamGradeDao,
): ViewModel() {
    val screenState: StateFlow<DevScreenState> = combine(
        appConfigRepo.debugState,
        appConfigRepo.showDevState,
    ) { debug, showDev ->
        DevScreenState(
            debug = debug,
            showDev = showDev,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DevScreenState()
    )

    fun login(token: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch { appConfigRepo.saveToken(token); onComplete() }
    }

    fun copyTokenToClipboard(onToken: (String) -> Unit) {
        // TODO: Show "copied!" alert in some way
        viewModelScope.launch {
            appConfigRepo.getToken()?.let(onToken)
        }
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

    fun setDebug(value: Boolean) {
        viewModelScope.launch {
            appConfigRepo.setDebug(value)
        }
    }

    fun setShowDev(value: Boolean) {
        viewModelScope.launch {
            appConfigRepo.setShowDev(value)
        }
    }
}


