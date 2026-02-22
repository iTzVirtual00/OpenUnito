package me.itzvirtual.myunitox

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.itzvirtual.myunitox.ui.LocaleHelper
import me.itzvirtual.myunitox.ui.agenda.AgendaScreen
import me.itzvirtual.myunitox.ui.dashboard.DashboardScreen
import me.itzvirtual.myunitox.ui.devtools.DevtoolsScreen
import me.itzvirtual.myunitox.ui.examdates.ExamDatesScreen
import me.itzvirtual.myunitox.ui.home.HomeScreen
import me.itzvirtual.myunitox.ui.login.AuthViewModel
import me.itzvirtual.myunitox.ui.login.UniToLoginScreen
import me.itzvirtual.myunitox.ui.profile.ProfileScreen
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme

@AndroidEntryPoint
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateAppLanguage("it")
        setContent {
            RootNavigation()
        }
    }
}

@Composable
fun RootNavigation(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination = if (viewModel.isUserLoggedIn()) "main" else "login"

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
                    viewModel.login(token)
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true } // it destroys
                    }
                },
                startUrl = startUrl,
                modifier = Modifier.safeDrawingPadding()
            )
        }

        // --- MAIN APP VIEW ---
        composable("main") { backStackEntry ->
            MyUnitoXTheme {
                MyUniTOXApp(
                    onSessionExpired = {
                        viewModel.clearToken()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MyUniTOXApp(onSessionExpired: () -> Unit = {}) {
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
            when (currentDestination) {
                AppDestinations.HOME -> HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToSchedule = { currentDestination = AppDestinations.CALENDAR }
                )

                AppDestinations.DASHBOARD -> DashboardScreen(
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestinations.CALENDAR -> AgendaScreen(
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.PROFILE -> ProfileScreen(
                    modifier = Modifier.padding(innerPadding)
                )

                AppDestinations.DEVTOOLS -> DevtoolsScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    DASHBOARD("Old", Icons.Default.Home),
    PROFILE("Profile", Icons.Default.AccountBox),
    CALENDAR("Calendar", Icons.Default.CalendarToday),
    DEVTOOLS("Dev", Icons.Default.LogoDev),
}
