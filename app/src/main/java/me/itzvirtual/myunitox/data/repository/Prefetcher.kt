package me.itzvirtual.myunitox.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefetcher @Inject constructor(
//    private val studentDataRepo: StudentDataRepository, // prefetched by UnitoAPI module

    private val agendaRepo: AgendaRepository,
    private val dashboardDataRepo: DashboardDataRepository,
    private val examDateRepo: ExamDateRepository,
    private val examResultRepo: ExamResultRepository,
    private val gradeRepository: GradeRepository,

    private val appConfigRepo: AppConfigRepository,
) {
    fun prefetchData(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            // Run multiple prefetches in parallel
            val jobs = listOf(
                launch {
                    val now = LocalDate.now()
                    val startDate = now.with(java.time.DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant()
                    val endDate = now.with(java.time.DayOfWeek.SUNDAY).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

                    val calendarLinkId = appConfigRepo.getCalendarLinkId()
                    val calendarClientId = appConfigRepo.getCalendarClientId()

                    if (calendarLinkId != null) {
                        agendaRepo.fetchPublicAgenda(
                            calendarLinkId,
                            calendarClientId,
                            startDate,
                            endDate
                        )
                    }
                },
                launch { dashboardDataRepo.fetchDashboardData() },
                launch { examDateRepo.fetchExamDates() },
                launch { examResultRepo.fetchExamResults() },
                launch { gradeRepository.fetchGrades() }
            )

            joinAll(*jobs.toTypedArray())
            println("Prefetching complete!")
        }
    }
}