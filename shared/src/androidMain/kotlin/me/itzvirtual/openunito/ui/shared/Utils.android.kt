package me.itzvirtual.openunito.ui.shared

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.system.exitProcess

@Composable
actual fun rememberRestartApp(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            val launchIntent = context.packageManager
                .getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                context.startActivity(Intent.makeRestartActivityTask(launchIntent.component))
            }
            exitProcess(0)
        }
    }
}