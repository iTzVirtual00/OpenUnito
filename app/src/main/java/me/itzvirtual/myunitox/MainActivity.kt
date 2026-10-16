// MainActivity.kt
package me.itzvirtual.myunitox

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import me.itzvirtual.openunito.core.network.NetworkMonitor
import me.itzvirtual.openunito.data.Prefetcher
import me.itzvirtual.openunito.data.calendar.local.CalendarConfigDao
import me.itzvirtual.openunito.data.config.local.AppConfigDao
import me.itzvirtual.openunito.ui.shared.LocaleHelper
import org.koin.android.ext.android.inject
import org.koin.android.annotation.KoinViewModel

//TODO: when switching tabs, it doesn't remember if profile or home tab was set
class MainActivity : ComponentActivity() {

    private val appConfigDao: AppConfigDao by inject()
    private val calendarConfigDao: CalendarConfigDao by inject()

    override fun attachBaseContext(newBase: Context) {
        // This forces the saved language into the Activity's context
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private fun updateAppLanguage(newLang: String) {
        val currentLang = resources.configuration.locales[0].language

        if (currentLang != newLang) {
            _root_ide_package_.me.itzvirtual.openunito.ui.shared.LocaleHelper.setLocale(this, newLang)
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
        enableEdgeToEdge()
        updateAppLanguage("it")
        setContent {
            _root_ide_package_.me.itzvirtual.openunito.ui.RootNavigation()
        }
    }
}

@KoinViewModel
class MainViewModel(
    private val prefetcher: Prefetcher,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val isOnline = networkMonitor.isOnline
    fun prefetchData(scope: CoroutineScope) = prefetcher.prefetchData(scope)
}
