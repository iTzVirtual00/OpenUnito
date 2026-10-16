package me.itzvirtual.openunito.ui.themes.simpleblue

import androidx.compose.material3.Typography

val AppTypography = Typography().scale(1f)
fun Typography.scale(factor: Float): Typography {
    return Typography(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * factor),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * factor),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * factor),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * factor),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * factor),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * factor),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * factor),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * factor),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * factor),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * factor),
        bodyMedium = bodyMedium.copy(fontSize = bodyMedium.fontSize * factor),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * factor),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * factor),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * factor),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * factor)
    )
}
