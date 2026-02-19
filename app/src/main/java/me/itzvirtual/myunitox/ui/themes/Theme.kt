package me.itzvirtual.myunitox.ui.themes

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun MyUnitoXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {

    // https://stackoverflow.com/a/77041136
    val customColorsPalette = if (darkTheme) me.itzvirtual.myunitox.ui.themes.main.DarkCustomColorsPalette else me.itzvirtual.myunitox.ui.themes.main.LightCustomColorsPalette
    CompositionLocalProvider(
        me.itzvirtual.myunitox.ui.themes.main.LocalCustomColorsPalette provides customColorsPalette
    ) {
        me.itzvirtual.myunitox.ui.themes.main.MyUnitoXTheme1(content = content)
    }
}