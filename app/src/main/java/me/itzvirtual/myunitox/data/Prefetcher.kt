package me.itzvirtual.myunitox.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.config.AppConfigRepository
import me.itzvirtual.myunitox.data.dashboard.DashboardRepository
import me.itzvirtual.myunitox.data.examdate.ExamDateRepository
import me.itzvirtual.myunitox.data.examresult.ExamResultRepository
import me.itzvirtual.myunitox.data.grade.GradeRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefetcher @Inject constructor(
    private val agendaRepo: AgendaRepository,
    private val dashboardRepo: DashboardRepository,
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
    private val gradeRepository: GradeRepository,
    private val appConfigRepo: AppConfigRepository,
) {
    fun prefetchData(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val jobs = listOf(
                launch {
                    val now = LocalDate.now()
                    val startDate = now.with(DayOfWeek.MONDAY)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val endDate = now.with(DayOfWeek.SUNDAY).plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val calendarLinkId = appConfigRepo.getCalendarLinkId()
                    val calendarClientId = appConfigRepo.getCalendarClientId()
                    if (calendarLinkId != null) {
                        agendaRepo.refresh(calendarLinkId, calendarClientId, startDate, endDate)
                    }
                },
                launch { dashboardRepo.refresh() },
                launch { examDateRepo.refresh() },
                launch { examResultRepo.refresh() },
                launch { gradeRepository.refresh() },
            )
            joinAll(*jobs.toTypedArray())
        }
    }
}
