package me.itzvirtual.openunito.ui.shared

import android.content.Context
import android.content.res.Configuration

import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.edit

// ai bullshit
object LocaleHelper {
    private const val PREFS_NAME = "unitox_settings"
    private const val KEY_LANG = "selected_language"

    /**
     * Use this in attachBaseContext to force the saved language
     */
    fun onAttach(context: Context): Context {
        val lang = getSavedLanguage(context)
        Log.i("OpenUnito", "LocaleHelper saved locale: $lang")
        return updateResources(context, lang)
    }

    /**
     * Saves the language and returns the new context
     */
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    /**
     * Bridge: Converts Compose Locale to Java Locale for this helper
     */
    fun setLocale(context: Context, composeLocale: Locale): Context {
        // Use .platformLocale for Compose 1.7.0+
        return setLocale(context, composeLocale.platformLocale.language)
    }

    /**
     * Returns the currently saved Java Locale
     */
    fun getLocale(context: Context): java.util.Locale {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(KEY_LANG, java.util.Locale.getDefault().language) ?: "en"
        return java.util.Locale.forLanguageTag(lang) // Use the modern forLanguageTag
    }

    /**
     * Returns the currently saved Compose Locale
     */
    fun getComposeLocale(context: Context): Locale {
        return Locale(getLocale(context).toLanguageTag())
    }

    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, java.util.Locale.getDefault().language) ?: "en"
    }

    private fun persist(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_LANG, language) }
    }

    private fun updateResources(context: Context, language: String): Context {
        // 1. Use forLanguageTag instead of the Locale(language) constructor
        Log.i("OpenUnito", "LocaleHelper updating resource to: $language")
        val locale = java.util.Locale.forLanguageTag(language)
        java.util.Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        // 2. Apply the locale to the configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        // 3. Create the configuration context for API 17+
        return context.createConfigurationContext(configuration)
    }
}