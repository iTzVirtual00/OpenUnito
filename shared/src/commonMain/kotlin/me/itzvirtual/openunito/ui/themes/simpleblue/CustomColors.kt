package me.itzvirtual.openunito.ui.themes.simpleblue

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsPalette(
    val gradePassed: Color = Color.Unspecified,
    val gradeNotPassed: Color = Color.Unspecified,
    val customizationColor: Color = Color.Unspecified,
    val languageColor: Color = Color.Unspecified,
    val devColor: Color = Color.Unspecified,
)

val GradePassedLight = Color(color = 0xFF0DBE3C)
val GradeNotPassedLight = Color(color = 0xFFBD4E12)

val GradePassedDark = Color(color = 0xFF2CE75B)
val GradeNotPassedDark = Color(color = 0xFFBD4E12)
val CustomizationColorDark = Color(1.0f, 0.388f, 0.239f, 1.0f) // TODO: add onCust. if you want to use it on topBar or else
val LanguageColorDark = Color(0.624f, 0.42f, 0.953f, 1.0f)
val DevColorDark = Color(0.353f, 0.902f, 0.533f, 1.0f)

val LightCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedLight,
    gradeNotPassed = GradeNotPassedLight,
    customizationColor = CustomizationColorDark,
    languageColor = LanguageColorDark,
    devColor = DevColorDark,
)

val DarkCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedDark,
    gradeNotPassed = GradeNotPassedDark,
    customizationColor = CustomizationColorDark,
    languageColor = LanguageColorDark,
    devColor = DevColorDark,
)