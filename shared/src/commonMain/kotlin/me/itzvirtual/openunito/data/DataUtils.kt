package me.itzvirtual.openunito.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import me.itzvirtual.openunito.data.agenda.local.AgendaDao
import me.itzvirtual.openunito.data.dashboard.local.DashboardDao
import me.itzvirtual.openunito.data.examdate.local.ExamDateDao
import me.itzvirtual.openunito.data.examresult.local.ExamResultDao
import me.itzvirtual.openunito.data.grade.local.ExamGradeDao
import me.itzvirtual.openunito.data.student.local.StudentDao
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class DataUtils(
    private val agendaDao: AgendaDao,
    private val dashboardDao: DashboardDao,
    private val studentDao: StudentDao,
    private val examResultDao: ExamResultDao,
    private val examDateDao: ExamDateDao,
    private val examGradeDao: ExamGradeDao,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun purgeAllCaches() = withContext(ioDispatcher) {
        listOf(
            async { agendaDao.clearAll() },
            async { dashboardDao.clearAll() },
            async { studentDao.clearAll() },
            async { examResultDao.clearAll() },
            async { examDateDao.clearAll() },
            async { examGradeDao.clearAll() }
        ).awaitAll()
    }
}