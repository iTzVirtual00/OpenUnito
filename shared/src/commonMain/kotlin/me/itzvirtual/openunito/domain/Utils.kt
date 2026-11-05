@file:OptIn(FormatStringsInDatetimeFormats::class)

package me.itzvirtual.openunito.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
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


class ConcurrentLruMap<K, V>(@PublishedApi internal val maxSize: Int) {
    // LinkedHashMap keeps insertion order
    // first is the oldest, last is the newest
    // PublishedApi is needed to allow `inline` in `getOrPut`
    @PublishedApi
    internal val map = LinkedHashMap<K, V>()
    @PublishedApi
    internal val mutex = Mutex()

    suspend fun get(key: K): V? = mutex.withLock {
        val value = map.remove(key) ?: return null
        map[key] = value // move to end
        value
    }

    suspend fun put(key: K, value: V) = mutex.withLock {
        map.remove(key)
        map[key] = value
        if (map.size > maxSize) {
            val eldestKey = map.keys.first()
            map.remove(eldestKey)
        }
    }

    suspend fun remove(key: K): V? = mutex.withLock {
        map.remove(key)
    }

    suspend fun clear() = mutex.withLock {
        map.clear()
    }

    suspend fun size(): Int = mutex.withLock {
        map.size
    }

    // inline
    // https://kotlinlang.org/docs/inline-functions.html
    // https://kotlinlang.org/docs/inline-functions.html#non-local-jump-expressions
    suspend inline fun getOrPut(key: K, crossinline defaultValue: suspend () -> V): V {
        // fast
        mutex.withLock {
            val existing = map.remove(key)
            if (existing != null) {
                map[key] = existing
                return existing
            }
        }

        // compute
        val value = defaultValue()

        mutex.withLock {
            // prioritize fast path behavior
            val existing = map.remove(key)
            if (existing != null) {
                map[key] = existing
                return existing
            }

            map[key] = value
            if (map.size > maxSize) {
                map.remove(map.keys.first())
            }
            return value
        }
    }
}