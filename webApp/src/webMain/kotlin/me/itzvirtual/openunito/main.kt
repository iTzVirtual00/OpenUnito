package openunito.example.openunito

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import me.itzvirtual.openunito.initKoin
import me.itzvirtual.openunito.ui.RootNavigation

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    ComposeViewport {
        RootNavigation()
    }
}