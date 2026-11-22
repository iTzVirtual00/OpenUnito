package me.itzvirtual.openunito.ui.themes.simpleblue

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Immutable
data class CustomColorsPalette(
    val gradePassed: Color = Color.Unspecified,
    val gradeNotPassed: Color = Color.Unspecified,

    val homeColor: Color = Color.Unspecified,
    val onHomeColor: Color = Color.Unspecified,

    val careerColor: Color = Color.Unspecified,
    val onCareerColor: Color = Color.Unspecified,

    val agendaColor: Color = Color.Unspecified,
    val onAgendaColor: Color = Color.Unspecified,

    val settingsColor: Color = Color.Unspecified,
    val onSettingsColor: Color = Color.Unspecified,

    val customizationColor: Color = Color.Unspecified,
    val languageColor: Color = Color.Unspecified,
    val devColor: Color = Color.Unspecified,
)

val GradePassedLight = Color(color = 0xFF0DBE3C)
val GradeNotPassedLight = Color(color = 0xFFBD4E12)

val GradePassedDark = Color(color = 0xFF4ACC66)
val GradeNotPassedDark = Color(color = 0xFFBD4E12)

// Screen Colors
val HomeColor = Color(0xFF977BE5)
val OnHomeColor = lerp(
    HomeColor,
    Color.Black,
    0.4f
)

val CareerColor = Color(0xFF977BE5)
val OnCareerColor = lerp(
    CareerColor,
    Color.Black,
    0.4f
)

val AgendaColor = Color(0xFFDE8129)
val OnAgendaColor = lerp(
    AgendaColor,
    Color.Black,
    0.15f
)

val SettingsColor = Color(0xFF977BE5)
val OnSettingsColor = lerp(
    SettingsColor,
    Color.Black,
    0.3f
)

// SettingsScreen Icon Colors
val CustomizationColorDark = Color(1.0f, 0.388f, 0.239f, 1.0f)
val LanguageColorDark = Color(0.624f, 0.42f, 0.953f, 1.0f)
val DevColorDark = Color(0.353f, 0.902f, 0.533f, 1.0f)

val LightCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedDark,
    gradeNotPassed = GradeNotPassedDark,

    homeColor = HomeColor,
    onHomeColor = OnHomeColor,

    careerColor = CareerColor,
    onCareerColor = OnCareerColor,

    agendaColor = AgendaColor,
    onAgendaColor = OnAgendaColor,

    settingsColor = SettingsColor,
    onSettingsColor = OnSettingsColor,

    customizationColor = CustomizationColorDark,
    languageColor = LanguageColorDark,
    devColor = DevColorDark,
)

val DarkCustomColorsPalette = CustomColorsPalette(
    gradePassed = GradePassedDark,
    gradeNotPassed = GradeNotPassedDark,

    homeColor = HomeColor,
    onHomeColor = OnHomeColor,

    careerColor = CareerColor,
    onCareerColor = OnCareerColor,

    agendaColor = AgendaColor,
    onAgendaColor = OnAgendaColor,

    settingsColor = SettingsColor,
    onSettingsColor = OnSettingsColor,

    customizationColor = CustomizationColorDark,
    languageColor = LanguageColorDark,
    devColor = DevColorDark,
)