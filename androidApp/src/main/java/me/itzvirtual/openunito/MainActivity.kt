package me.itzvirtual.openunito

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.text.intl.Locale
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.itzvirtual.openunito.ui.RootNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark( // Status bar color
                android.graphics.Color.argb(0.3f, 0f,0f,0f)
            )
        )

        val language = if (Locale.current.language.lowercase() == "it") "it" else "en"
        Log.d("MainActivity", "Language: $language" )

        setContent {
            RootNavigation()
        }
    }
}
