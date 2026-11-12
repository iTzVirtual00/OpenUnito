package me.itzvirtual.openunito.ui.shared

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
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

actual class CalendarLauncher(private val context: Context) {
    actual fun addEvent(event: CalendarEventModel) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endTimeMillis)
        }
        // Flags needed since it's being called from outside an Activity context
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Composable
actual fun rememberCalendarLauncher(): CalendarLauncher {
    val context = LocalContext.current
    return remember { CalendarLauncher(context) }
}