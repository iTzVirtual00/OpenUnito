package me.itzvirtual.openunito.data.localization

import dev.carcara.kotlinx.locale.Locale

data class SupportedLanguage(
    val tag: String,
    val displayName: String = "",
    val isCustom: Boolean = false,
)

data class SupportedRegion(
    val regionCode: String,
    val displayName: String = "",
)

object SupportedLocales {
    val languages: List<SupportedLanguage> = listOf(
        SupportedLanguage(
            tag = "",
        ),
        SupportedLanguage(
            tag = "it",
            displayName = "Italiano"
        ),
        SupportedLanguage(
            tag = "en",
            displayName = "English"
        ),
        SupportedLanguage(
            tag = "it-x-pugliese",
            displayName = "PUGLIËSAH",
            isCustom = true,
        )
    )

    val regions: List<SupportedRegion> = listOf(
        SupportedRegion(
            regionCode = "",
        ),
        SupportedRegion(
            regionCode = "IT",
            displayName = "Italy (IT)",
        ),
        SupportedRegion(
            regionCode = "US",
            displayName = "United States (US)",
        ),
    )

    // handles instances where the selected tag contains extension (-x-*)
    fun buildTag(languageTag: String, regionTag: String): String {
        if (languageTag.isBlank()) return ""

        // Unspecified region makes Locale library use system
        if (regionTag.isEmpty()) return languageTag

        return if (languageTag.contains("-x-")) {
            val base = languageTag.substringBefore("-x-")
            val ext = languageTag.substringAfter("-x-")
            "$base-$regionTag-x-$ext"
        } else {
            "$languageTag-$regionTag"
        }
    }

    // returns language, region
    fun decomposeLocale(locale: Locale): Pair<String, String> {
        val fullTag = locale.toLanguageTag()
        val region = locale.region.orEmpty()

        val langTag = if (fullTag.contains("-x-")) {
            val base = locale.language
            val ext = fullTag.substringAfter("-x-")
            "$base-x-$ext"
        } else {
            locale.language
        }

        return langTag to region
    }
}
