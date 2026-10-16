package me.itzvirtual.openunito.core.cache

object CacheConfig {
    const val DASHBOARD_STALE_MS = 15 * 60 * 1000L       // 15 minutes
    const val EXAM_RESULT_STALE_MS = 15 * 60 * 1000L     // 15 minutes
    const val EXAM_DATE_STALE_MS = 30 * 60 * 1000L       // 30 minutes
    const val GRADE_STALE_MS = 30 * 60 * 1000L           // 30 minutes
    const val AGENDA_STALE_MS = 15 * 60 * 1000L          // 15 minutes
    const val AGENDA_RECENT_MS =
        5 * 60 * 1000L           // 5 minutes future: skip refresh if within this
    const val AGENDA_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000L // 7 days
    const val STUDENT_STALE_MS = 30 * 60 * 1000L // 30 minutes
}
