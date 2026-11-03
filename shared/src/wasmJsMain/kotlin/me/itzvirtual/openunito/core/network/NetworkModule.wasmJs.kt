package me.itzvirtual.openunito.core.network

import kotlinx.browser.window
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun getSystemLanguage(): String {
    val fullTag = window.navigator.language
    return fullTag.substringBefore('-').ifEmpty { "en" }
}

actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default

// TODO: allow user to use its own cors-proxy
actual val API_URL: String = "https://unito.app.cineca.it/"