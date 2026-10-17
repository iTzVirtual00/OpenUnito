package me.itzvirtual.openunito.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.window

@Composable
actual fun rememberRestartApp(): () -> Unit {
    return remember {
        {
            window.location.reload()
        }
    }
}