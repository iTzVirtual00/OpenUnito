package me.itzvirtual.openunito.data.localization

import dev.carcara.kotlinx.locale.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.itzvirtual.openunito.data.config.AppConfigRepository
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

data class ResolvedLocale(
    val langCode: String,
    val regionCode: String,

    val uiLocale: Locale,
    val formattingLocale: Locale,
    val systemLocale: Locale,

    val apiLanguage: String,
)

@Single
class AppLocaleManager(
    private val appConfigRepo: AppConfigRepository,
    @Provided private val platformLanguageManager: PlatformLanguageManager,
    @Named("ApplicationScope") private val externalScope: CoroutineScope,
) {
    init {
        externalScope.launch {
            appConfigRepo.selectedLocaleState
                .filterNotNull()
                .map { (lang, _) -> lang }
                .distinctUntilChanged()
                // Skips the initial database emission
                // we could also handle this in setDisplayLanguage
                //   it would check if the to-set language is the current one
                // keeping this for now
                .drop(1)
                .collect { langTag ->
                    platformLanguageManager.setDisplayLanguage(
                        SupportedLanguage(tag = langTag)
                    )
                }
        }
    }

    private val safeApiLanguages = setOf("en", "it")
    private val fallbackApiLanguage = "en"

    // TODO: also react to system language changes
    val localeState: StateFlow<ResolvedLocale?> = appConfigRepo.selectedLocaleState.filterNotNull()
        .map { (lang, region) -> resolveTag(lang, region) }
        .stateIn(
            scope = externalScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun resolveTag(langCode: String, regionCode: String): ResolvedLocale {
        val systemLocale = Locale.current

        val effectiveLang = langCode.ifBlank { systemLocale.language }
        val uiLocale = Locale.forLanguageTag(effectiveLang)

        val effectiveFormattingLang = regionCode.ifBlank { systemLocale.language }
        val formattingLocale = Locale.forLanguageTag(effectiveFormattingLang)

        val baseLang = uiLocale.language
        return ResolvedLocale(
            langCode = langCode,
            regionCode = regionCode,

            uiLocale = uiLocale,
            formattingLocale = formattingLocale,
            systemLocale = systemLocale,

            apiLanguage = if (baseLang.lowercase() in safeApiLanguages) baseLang else fallbackApiLanguage,
        )
    }

    suspend fun awaitLocale(): ResolvedLocale =
        localeState.filterNotNull().first()

    fun getCurrentLocaleSync(): ResolvedLocale = localeState.value ?: resolveTag("", "")
}

interface PlatformLanguageManager {
    fun setDisplayLanguage(lang: SupportedLanguage)
}
