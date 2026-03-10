package me.itzvirtual.myunitox.ui.feature_devtools

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.local.AgendaDao
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.dashboard.local.DashboardDao
import me.itzvirtual.myunitox.data.examdate.local.ExamDateDao
import me.itzvirtual.myunitox.data.examresult.local.ExamResultDao
import me.itzvirtual.myunitox.data.grade.local.ExamGradeDao
import me.itzvirtual.myunitox.data.student.local.StudentDao
import javax.inject.Inject

@HiltViewModel
class DevtoolsViewModel @Inject constructor(
    private val appConfigRepo: AppConfigRepository,
    private val agendaDao: AgendaDao,
    private val dashboardDao: DashboardDao,
    private val studentDao: StudentDao,
    private val examResultDao: ExamResultDao,
    private val examDateDao: ExamDateDao,
    private val examGradeDao: ExamGradeDao,
) : ViewModel() {

    fun login(token: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch { appConfigRepo.saveToken(token); onComplete() }
    }

    fun clearToken(onComplete: () -> Unit = {}) {
        viewModelScope.launch { appConfigRepo.clearAuth(); onComplete() }
    }

    fun copyTokenToClipboard(context: Context) {
        viewModelScope.launch {
            val token = appConfigRepo.getToken()
            if (token != null) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    android.content.ClipData.newPlainText(
                        "UnitoX Token",
                        token
                    )
                )
                Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
            }
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
}
