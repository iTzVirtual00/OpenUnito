package me.itzvirtual.myunitox.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.itzvirtual.myunitox.core.network.ApplicationScope
import me.itzvirtual.myunitox.core.network.IoDispatcher
import me.itzvirtual.myunitox.data.agenda.AgendaRepository
import me.itzvirtual.myunitox.data.calendar.CalendarConfigRepository
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
    private val calendarConfigRepo: CalendarConfigRepository,

    @param:ApplicationScope private val scope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun prefetchData(scope: CoroutineScope) {
        // Note: we are not prefetching student data as it is initialized with UnitoApi bullshit
        // Check StudentRepository.refresh() for more info
        scope.launch(ioDispatcher) {
            val globalWarmingJobs = listOf(
                launch { dashboardRepo.dashboardCacheFlow.first() },
                launch { examDateRepo.examDatesCacheFlow.first() },
                launch { examResultRepo.examResultsCacheFlow.first() },
                launch { gradeRepository.gradesFlow.first() }
            )

            val agendaJob = launch {
                val calendar = calendarConfigRepo.selectedCalendarFlow.first()
                if (calendar != null) {
                    // weekly prefetch
                    val now = LocalDate.now()

                    val start =
                        now.with(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val end =
                        now.with(DayOfWeek.SUNDAY).plusDays(1).atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                    agendaRepo.getAgendaFlow(calendar.calendarLinkId, start, end).first()

                    // today and tomorrow prefetch
                    val startOfToday = now.atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val endOfTomorrow =
                        now.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()
                    agendaRepo.getAgendaFlow(calendar.calendarLinkId, startOfToday, endOfTomorrow)
                        .first()
                }
            }

            (globalWarmingJobs + agendaJob).joinAll()
        }
    }
}
