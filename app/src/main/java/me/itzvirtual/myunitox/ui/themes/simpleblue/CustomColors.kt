package me.itzvirtual.myunitox.ui.themes.simpleblue

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsPalette(
    val gradePassed: Color = Color.Unspecified,
    val gradeNotPassed: Color = Color.Unspecified,
)

val GradePassedLight = Color(color = 0xFF0DBE3C)
val GradeNotPassedLight = Color(color = 0xFFBD4E12)

val GradePassedDark = Color(color = 0xFF2CE75B)
val GradeNotPassedDark = Color(color = 0xFFBD4E12)

val LightCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedLight,
    gradeNotPassed = GradeNotPassedLight,
)

val DarkCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedDark,
    gradeNotPassed = GradeNotPassedDark,
)