package me.itzvirtual.openunito.ui.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class SettingsViewModel (
    private val appConfigRepo: AppConfigRepository,
    private val agendaDao: AgendaDao,
    private val dashboardDao: DashboardDao,
    private val studentDao: StudentDao,
    private val examResultDao: ExamResultDao,
    private val examDateDao: ExamDateDao,
    private val examGradeDao: ExamGradeDao,
): ViewModel() {
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