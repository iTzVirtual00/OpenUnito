package me.itzvirtual.myunitox.ui.themes.main

import androidx.compose.runtime.Immutable
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


val CalendarBucket1 = Color(0xFF1E1F22)
val CalendarBucket2 = Color(0xFFE91E63)
val CalendarBucket3 = Color(0xFF4CAF50)
val CalendarBucket4 = Color(0xFF2196F3)
val CalendarBucket5 = Color(0xFFFF9800)
val CalendarBucket6 = Color(0xFF00BCD4)
val CalendarBucket7 = Color(0xFF795548)
val CalendarBucket8 = Color(0xFF607D8B)
val CalendarBucket9 = Color(0xFFF44336)
val CalendarBucket10 = Color(0xFF8BC34A)
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
