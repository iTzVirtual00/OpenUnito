package me.itzvirtual.openunito.domain

import android.app.LocaleManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import me.itzvirtual.openunito.data.localization.PlatformLanguageManager
import me.itzvirtual.openunito.data.localization.SupportedLanguage
import kotlin.jvm.java

class AndroidClipboardManagerImpl(
    private val context: Context
) : PlatformClipboardManager {
    override fun setText(text: String, label: String, sensitive: Boolean) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        if (sensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard?.setPrimaryClip(clip)
    }
}

class AndroidLanguageManagerImpl(private val context: Context) : PlatformLanguageManager {
    override fun setDisplayLanguage(lang: SupportedLanguage) {
        val languageTag = lang.tag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = if (languageTag.isEmpty()) {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(languageTag)
            }
        } else {
            // Android 5.0 – 12
            val localeList = if (languageTag.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(languageTag)
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }
}