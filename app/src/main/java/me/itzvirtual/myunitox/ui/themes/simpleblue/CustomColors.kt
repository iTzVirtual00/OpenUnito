package me.itzvirtual.myunitox.ui.themes.simpleblue

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsPalette(
    val gradePassed: Color = Color.Unspecified,
    val gradeNotPassed: Color = Color.Unspecified,
    val calendarBucket: List<Color> = listOf(Color.Unspecified)
)

val GradePassedLight = Color(color = 0xFF0DBE3C)
val GradeNotPassedLight = Color(color = 0xFFBD4E12)

val GradePassedDark = Color(color = 0xFF2CE75B)
val GradeNotPassedDark = Color(color = 0xFFBD4E12)

// HSV representation
val CalendarBucket1 = Color.hsv(220f, 0.07f, 0.13f)
val CalendarBucket2 = Color.hsv(340f, 0.85f, 0.64f)
val CalendarBucket3 = Color.hsv(122f, 0.69f, 0.6f)
val CalendarBucket4 = Color.hsv(207f, 1f, 0.87f)
val CalendarBucket5 = Color.hsv(36f, 1.00f, 0.6f)
val CalendarBucket6 = Color.hsv(187f, 1.00f, 0.6f)
val CalendarBucket7 = Color.hsv(16f, 0.44f, 0.6f)
val CalendarBucket8 = Color.hsv(200f, 0.23f, 0.6f)
val CalendarBucket9 = Color.hsv(4f, 0.78f, 0.6f)
val CalendarBucket10 = Color.hsv(88f, 0.63f, 0.6f)
val CalendarBucket = listOf(
    CalendarBucket1,
    CalendarBucket2,
    CalendarBucket3,
    CalendarBucket4,
    CalendarBucket5,
    CalendarBucket6,
    CalendarBucket7,
    CalendarBucket8,
    CalendarBucket9,
    CalendarBucket10
)

val LightCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedLight,
    gradeNotPassed = GradeNotPassedLight,
    calendarBucket = CalendarBucket,
)

val DarkCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedDark,
    gradeNotPassed = GradeNotPassedDark,
    calendarBucket = CalendarBucket,
)