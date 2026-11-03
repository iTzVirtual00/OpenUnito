package me.itzvirtual.openunito.core.network

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun getSystemLanguage(): String {
    val fullTag = window.navigator.language
    return fullTag.substringBefore('-').ifEmpty { "en" }
}

actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default
actual val API_URL: String = "REDACTED_PROXY_URL"