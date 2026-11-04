package me.itzvirtual.openunito

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.text.intl.Locale
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.itzvirtual.openunito.ui.RootNavigation
import me.itzvirtual.openunito.ui.shared.LocaleHelper
import org.koin.core.logger.Logger

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // This forces the saved language into the Activity's context
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private fun updateAppLanguage(newLang: String) {
        val currentLang = resources.configuration.locales[0].language

        if (currentLang != newLang) {
            LocaleHelper.setLocale(this, newLang)
            finish()

            val refreshIntent = Intent(this, MainActivity::class.java).apply {
                // echo custom extras if needed
                // putExtra("some_key", intent.getStringExtra("some_key"))

                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(refreshIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
            } else {
                @Suppress("DEPRECATION")
                overridePendingTransition(0, 0)
            }
        }
    }

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
        updateAppLanguage(language)
        setContent {
            RootNavigation()
        }
    }
}
