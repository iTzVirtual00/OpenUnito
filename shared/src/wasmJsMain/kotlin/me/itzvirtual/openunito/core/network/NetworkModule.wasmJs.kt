package me.itzvirtual.openunito.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun getSystemLanguage(): String {
    // TODO: get system language wasm
    return "it"
}

actual val platformIoDispatcher: CoroutineDispatcher = Dispatchers.Default
actual val API_URL: String = "REDACTED_PROXY_URL"