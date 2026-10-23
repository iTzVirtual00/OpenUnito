package me.itzvirtual.openunito.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.plus
import me.itzvirtual.openunito.data.agenda.AgendaRepository
import me.itzvirtual.openunito.data.calendar.CalendarConfigRepository
import me.itzvirtual.openunito.data.dashboard.DashboardRepository
import me.itzvirtual.openunito.data.examdate.ExamDateRepository
import me.itzvirtual.openunito.data.examresult.ExamResultRepository
import me.itzvirtual.openunito.data.grade.GradeRepository
import me.itzvirtual.openunito.domain.atStartOfDay
import me.itzvirtual.openunito.domain.getDayOfWeek
import me.itzvirtual.openunito.domain.getToday
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class Prefetcher(
    private val agendaRepo: AgendaRepository,
    private val dashboardRepo: DashboardRepository,
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
    private val gradeRepository: GradeRepository,
    private val calendarConfigRepo: CalendarConfigRepository,

    @Named("ApplicationScope") private val scope: CoroutineScope,
    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    // TODO: Fix this, recent changes to how Flow works, makes it return loading (null) immediately
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
                    val monday = getDayOfWeek(DayOfWeek.MONDAY)
                    val start = monday.atStartOfDay()
                    val end = monday.plus(7, DateTimeUnit.DAY).atStartOfDay()
                    agendaRepo.getAgendaFlow(calendar.calendarLinkId, start, end).first()

                    // today and tomorrow prefetch
                    val today = getToday()
                    val startOfToday = today.atStartOfDay()
                    val endOfTomorrow = today.plus(2, DateTimeUnit.DAY).atStartOfDay()
                    agendaRepo.getAgendaFlow(calendar.calendarLinkId, startOfToday, endOfTomorrow)
                        .first()
                }
            }

            (globalWarmingJobs + agendaJob).joinAll()
        }
    }
}
