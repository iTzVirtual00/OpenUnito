package me.itzvirtual.myunitox

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LogoDev
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import me.itzvirtual.myunitox.core.network.NetworkMonitor
import me.itzvirtual.myunitox.data.Prefetcher
import me.itzvirtual.myunitox.data.calendar.local.CalendarConfigDao
import me.itzvirtual.myunitox.data.config.local.AppConfigDao
import me.itzvirtual.myunitox.ui.feature_agenda.AgendaScreen
import me.itzvirtual.myunitox.ui.feature_devtools.DevtoolsScreen
import me.itzvirtual.myunitox.ui.feature_home.HomeScreen
import me.itzvirtual.myunitox.ui.feature_login.AuthViewModel
import me.itzvirtual.myunitox.ui.feature_login.UniToLoginScreen
import me.itzvirtual.myunitox.ui.feature_profile.ProfileScreen
import me.itzvirtual.myunitox.ui.shared.LocaleHelper
import me.itzvirtual.myunitox.ui.shared.OfflineBanner
import me.itzvirtual.myunitox.ui.shared.cleanupLegacyStorage
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var appConfigDao: AppConfigDao

    @Inject
    lateinit var calendarConfigDao: CalendarConfigDao

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
        enableEdgeToEdge()
        updateAppLanguage("it")
        cleanupLegacyStorage(
            context = this,
            appConfigDao = appConfigDao,
            calendarConfigDao = calendarConfigDao
        )
        setContent {
            RootNavigation()
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefetcher: Prefetcher,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val isOnline = networkMonitor.isOnline
    fun prefetchData(scope: CoroutineScope) = prefetcher.prefetchData(scope)
}

@Composable
fun RootNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    // null means still loading from DB wait before deciding
    if (isAuthenticated == null) return

    val startDestination = if (isAuthenticated == true) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        // --- LOGIN VIEW ---
        composable("login") {
            val startUrl = remember {
                val state =
                    (('A'..'Z') + ('a'..'z') + ('0'..'9')).shuffled().take(40).joinToString("")
                "https://idp.unito.it/idp/profile/oidc/authorize?response_type=code&client_id=https://unito.app.cineca.it/oidc&redirect_uri=https://unito.app.cineca.it/oauth/app-auth-code/unito-prod&scope=openid%20offline_access&state=${state}"
            }
            UniToLoginScreen(
                onAuthSuccess = { token ->
                    Log.d("MyUnitoX", "Received token: $token")
                    authViewModel.login(token) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true } // it destroys
                        }
                    }
                },
                startUrl = startUrl,
                modifier = Modifier.safeDrawingPadding()
            )
        }

        // --- MAIN APP VIEW ---
        composable("main") { backStackEntry ->
            val scope = rememberCoroutineScope()
            val model: MainViewModel = hiltViewModel()
            model.prefetchData(scope)
            val isOnline by model.isOnline.collectAsState(initial = true)
            MyUnitoXTheme {
                MyUniTOXApp(
                    isOnline = isOnline,
                    onSessionExpired = {
                        authViewModel.clearToken {
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MyUniTOXApp(
    isOnline: Boolean = true,
    onSessionExpired: () -> Unit = {},
) {
    Log.d("MyUnitoX", "Current locale: ${LocalConfiguration.current.locales[0].language}")
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize()) {
                // why is this so fucking easy
                AnimatedContent(
                    targetState = currentDestination,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally { it } + fadeIn())
                                .togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn())
                                .togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "ScreenTransition"
                ) { targetDestination ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (targetDestination) {
                            AppDestinations.HOME -> HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onNavigateToSchedule = {
                                    currentDestination = AppDestinations.CALENDAR
                                }
                            )

//                            AppDestinations.DASHBOARD -> DashboardScreen(
//                                modifier = Modifier.padding(innerPadding),
//                            )

                            AppDestinations.CALENDAR -> AgendaScreen(
                                modifier = Modifier.padding(innerPadding)
                            )

                            AppDestinations.PROFILE -> ProfileScreen(
                                modifier = Modifier.padding(innerPadding),
                            )

                            AppDestinations.DEVTOOLS -> DevtoolsScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }

                OfflineBanner(isOnline = isOnline)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    PROFILE("Profile", Icons.Default.AccountBox),
    CALENDAR("Calendar", Icons.Default.CalendarToday),
    DEVTOOLS("Dev", Icons.Default.LogoDev),
}
