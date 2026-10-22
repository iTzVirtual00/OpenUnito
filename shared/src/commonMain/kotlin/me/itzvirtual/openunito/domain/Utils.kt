@file:OptIn(FormatStringsInDatetimeFormats::class)

package me.itzvirtual.openunito.domain

import dev.carcara.kotlinx.locale.Locale
import dev.carcara.kotlinx.locale.datetime.FormatStyle
import dev.carcara.kotlinx.locale.datetime.cldr.format
import dev.carcara.kotlinx.locale.datetime.cldr.skeletons.format
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
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

// TODO: replace Locale.current with actual selected app locale (and reactive)

// 12 Oct
fun LocalDate.formatFriendlyDayMonth(
    locale: Locale = Locale.current
): String = this.format("dMMM", locale)

// Mon 12
fun LocalDate.formatFriendlyDayOfWeek(
    locale: Locale = Locale.current
): String = this.format("Ed", locale)

// Monday 12 October 2026
fun LocalDate.formatFriendlyFullDate(
    locale: Locale = Locale.current
): String = this.format("yMMMMEEEEd", locale)

// "14:30" or "2:30 PM"
fun Instant.formatFriendlyTime(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    locale: Locale = Locale.current,
): String = this.toLocalDateTime(timeZone).time.formatFriendlyTime(locale)
fun LocalTime.formatFriendlyTime(
    locale: Locale = Locale.current,
): String = this.format(FormatStyle.SHORT, locale)

// "21 dec 2026"
fun Instant.formatMediumDate(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    locale: Locale = Locale.current,
): String = this.toLocalDateTime(timeZone).date.formatMediumDate(locale)
fun LocalDate.formatMediumDate(
    locale: Locale = Locale.current,
): String = this.format(FormatStyle.MEDIUM, locale)

// "21/10/2026" or "10/21/2026"
fun Instant.formatDate(
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    locale: Locale = Locale.current,
): String = this.toLocalDateTime(timeZone).date.formatDate(locale)
fun LocalDate.formatDate(
    locale: Locale = Locale.current,
): String = this.format(FormatStyle.SHORT, locale)

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
interface PlatformClipboardManager {
    fun setText(text: String, label: String = "", sensitive: Boolean = false)
}

// MATH
fun Double.toRadians(): Double = this * PI / 180.0
fun Float.toRadians(): Float = (this * PI / 180f).toFloat()