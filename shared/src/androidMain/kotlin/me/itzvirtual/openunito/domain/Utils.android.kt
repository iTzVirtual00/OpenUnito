package me.itzvirtual.openunito.domain

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.ui.platform.ClipEntry

actual fun plainTextClipEntry(text: String, sensitive: Boolean): ClipEntry {
    val clip = ClipData.newPlainText("text", text)
    if (sensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }
    return ClipEntry(clip)
}
