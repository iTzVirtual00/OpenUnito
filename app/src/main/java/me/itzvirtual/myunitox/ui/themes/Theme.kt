package me.itzvirtual.myunitox.ui.themes

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import me.itzvirtual.myunitox.ui.themes.simpleblue.*


val LocalCustomColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }

@Composable
fun MyUnitoXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    // https://stackoverflow.com/a/77041136
    val customColorsPalette = if (darkTheme) DarkCustomColorsPalette else LightCustomColorsPalette
    CompositionLocalProvider(
        LocalCustomColorsPalette provides customColorsPalette
    ) {
        AppTheme(dynamicColor=dynamicColor, content = content)
    }
}