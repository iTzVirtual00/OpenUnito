package me.itzvirtual.openunito.data.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import co.touchlab.kermit.Logger
import dev.carcara.kotlinx.locale.Locale
import dev.carcara.kotlinx.locale.datetime.FormatStyle
import dev.carcara.kotlinx.locale.datetime.cldr.format
import dev.carcara.kotlinx.locale.datetime.cldr.skeletons.format
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.itzvirtual.openunito.ui.LocalResolvedLocale
import kotlin.time.Instant

class DateFormatterScope(val locale: Locale = Locale.current) {
    // 12 Oct
    fun LocalDate.formatFriendlyDayMonth(): String = this.format("dMMM", locale)
    
    // Mon 12
    fun LocalDate.formatFriendlyDayOfWeek(): String = this.format("Ed", locale)
    
    // Monday 12 October 2026
    fun LocalDate.formatFriendlyFullDate(): String = this.format("yMMMMEEEEd", locale)
    
    // "14:30" or "2:30 PM"
    fun Instant.formatFriendlyTime(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String = this.toLocalDateTime(timeZone).time.formatFriendlyTime()
    
    fun LocalTime.formatFriendlyTime(): String = this.format(FormatStyle.SHORT, locale)
    
    // "21 dec 2026"
    fun Instant.formatMediumDate(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String = this.toLocalDateTime(timeZone).date.formatMediumDate()
    
    fun LocalDate.formatMediumDate(): String = this.format(FormatStyle.MEDIUM, locale)
    
    // "21/10/2026" or "10/21/2026"
    fun Instant.formatDate(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String = this.toLocalDateTime(timeZone).date.formatDate()
    
    fun LocalDate.formatDate(): String = this.format(FormatStyle.SHORT, locale)
    
    // 11
    fun Instant.formatFullMonth(
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String = this.toLocalDateTime(timeZone).date.formatFullMonth()
    
    fun LocalDate.formatFullMonth(): String = this.format("MMMM", locale)
}

// Better solutions are welcome :)
@Composable
@ReadOnlyComposable
inline fun <R> withDateFormatter(
    locale: Locale = LocalResolvedLocale.current.locale,
    block: DateFormatterScope.() -> R
): R = DateFormatterScope(locale).block() // Crazy features from kotlin right here

@Composable
@ReadOnlyComposable
fun rememberDateFormatter(
    locale: Locale = LocalResolvedLocale.current.locale
): DateFormatterScope = DateFormatterScope(locale)

