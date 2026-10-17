package me.itzvirtual.openunito.domain

import kotlinx.browser.window

class WasmJsClipboardManagerImpl : PlatformClipboardManager {
    @OptIn(ExperimentalWasmJsInterop::class)
    override fun setText(text: String, label: String, sensitive: Boolean) {
        window.navigator.clipboard.writeText(text)
    }
}