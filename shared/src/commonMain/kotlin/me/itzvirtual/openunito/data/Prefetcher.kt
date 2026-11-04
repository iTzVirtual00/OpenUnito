package me.itzvirtual.openunito.data

import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single

// Legacy code, will be removed in the future
@Single
class Prefetcher(
//    private val agendaRepo: AgendaRepository,
//    private val dashboardRepo: DashboardRepository,
//    private val examDateRepo: ExamDateRepository,
//    private val examResultRepo: ExamResultRepository,
//    private val gradeRepository: GradeRepository,
//    private val calendarConfigRepo: CalendarConfigRepository,
//
//    @Named("ApplicationScope") private val scope: CoroutineScope,
//    @Named("IoDispatcher") private val ioDispatcher: CoroutineDispatcher
) {
    fun prefetchData(scope: CoroutineScope) {
        return
//        scope.launch(ioDispatcher) {
//        }
    }
}
