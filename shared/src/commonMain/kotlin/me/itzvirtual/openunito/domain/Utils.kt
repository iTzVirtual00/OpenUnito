package me.itzvirtual.openunito.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

// TIME UTILS
fun getNowInstant(): Instant {
    return Clock.System.now()
}
fun getNowMillis(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

fun getToday(tz: TimeZone = getCurrentTimeZone()): LocalDate {
    return Clock.System.now().toLocalDateTime(tz).date
}

fun getCurrentTimeZone(): TimeZone {
    return TimeZone.currentSystemDefault()
}

val HHMMFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}
val DDMMYYYYFormat = LocalDate.Format {
    day()
    char('/')
    monthNumber()
    char('/')
    year()
}

// TODO: localize
private val italianShortMonths = MonthNames(
    "gen", "feb", "mar", "apr", "mag", "giu",
    "lug", "ago", "set", "ott", "nov", "dic",
)
val dayMonthFormatter = LocalDate.Format {
    day(Padding.NONE)
    char(' ')
    monthName(italianShortMonths)
}
private val italianShortDays = DayOfWeekNames(
    "lun", "mar", "mer", "gio", "ven", "sab", "dom",
)
val dayFormatter = LocalDate.Format {
    dayOfWeek(italianShortDays)
    char(' ')
    day(padding = Padding.NONE)
}

private val italianFullDays = DayOfWeekNames(
    "lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica",
)

private val italianFullMonths = MonthNames(
    "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
    "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre",
)
val dateFormat1 = LocalDate.Format {
    dayOfWeek(italianFullDays)
    char(' ')
    day(padding = Padding.NONE)
    char(' ')
    monthName(italianFullMonths)
    char(' ')
    year()
}

fun formatTime(
    instant: Instant,
    format: DateTimeFormat<LocalTime>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String = instant.toLocalDateTime(timeZone).time.format(format)

fun getDayOfWeek(
    day: DayOfWeek,
    today: LocalDate = getToday(),
): LocalDate = today.plus(day.isoDayNumber - today.dayOfWeek.isoDayNumber, DateTimeUnit.DAY)

fun LocalDate.atStartOfDay(tz: TimeZone = getCurrentTimeZone()) = this.atStartOfDayIn(tz)

fun Instant.toLocalDate(timeZone: TimeZone = getCurrentTimeZone()): LocalDate =
    toLocalDateTime(timeZone).date

fun Instant.toLocalTime(timeZone: TimeZone = getCurrentTimeZone()): LocalTime =
    toLocalDateTime(timeZone).time


// CLIPBOARD
expect fun plainTextClipEntry(text: String, sensitive: Boolean = false): ClipEntry

//expect fun ClipEntry.plainTextOrNull(): String?

class AppClipboard(private val clipboard: Clipboard) {
    suspend fun copyText(text: String, sensitive: Boolean = false) {
        clipboard.setClipEntry(plainTextClipEntry(text, sensitive))
    }
//    suspend fun pasteText(): String? = clipboard.getClipEntry()?.plainTextOrNull()
}

// ai-suggested shit
@Composable
fun rememberAppClipboard(): AppClipboard {
    val clipboard = LocalClipboard.current
    return remember(clipboard) { AppClipboard(clipboard) }
}