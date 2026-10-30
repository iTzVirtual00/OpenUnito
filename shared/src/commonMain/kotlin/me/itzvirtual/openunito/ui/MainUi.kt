package me.itzvirtual.openunito.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import me.itzvirtual.openunito.MainViewModel
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.calendar_tab_title
import me.itzvirtual.openunito.resources.career_tab_title
import me.itzvirtual.openunito.resources.home_tab_title
import me.itzvirtual.openunito.resources.settings_tab_title
import me.itzvirtual.openunito.ui.feature_agenda.AgendaScreen
import me.itzvirtual.openunito.ui.feature_career.CareerScreen
import me.itzvirtual.openunito.ui.feature_career.transcript_screen.TranscriptScreen
import me.itzvirtual.openunito.ui.feature_examsessions.ExamSessionScreen
import me.itzvirtual.openunito.ui.feature_home.HomeScreen
import me.itzvirtual.openunito.ui.feature_login.AuthViewModel
import me.itzvirtual.openunito.ui.feature_login.UniToLoginScreen
import me.itzvirtual.openunito.ui.feature_profile.ProfileScreen
import me.itzvirtual.openunito.ui.feature_settings.AppearanceScreen
import me.itzvirtual.openunito.ui.feature_settings.LanguageScreen
import me.itzvirtual.openunito.ui.feature_settings.SettingsScreen
import me.itzvirtual.openunito.ui.feature_settings.dev_screen.DevScreen
import me.itzvirtual.openunito.ui.shared.OfflineBanner
import me.itzvirtual.openunito.ui.shared.OpenUnitoNavState
import me.itzvirtual.openunito.ui.shared.rememberOpenUnitoNavState
import me.itzvirtual.openunito.ui.shared.tabAwareEnterTransition
import me.itzvirtual.openunito.ui.shared.tabAwareExitTransition
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.reflect.KClass

// TODO: add recovery points

// TODO: add a background gradient or smth
@Composable
fun RootNavigation(
    authViewModel: AuthViewModel = koinViewModel()
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
                    Logger.d(tag = "OpenUnito") { "Received token: $token" }
                    authViewModel.login(token) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true } // it destroys
                            restoreState = true
                        }
                    }
                },
                startUrl = startUrl,
                modifier = Modifier.safeDrawingPadding()
            )
        }

        // --- MAIN APP VIEW ---
        composable("main") {
            val scope = rememberCoroutineScope()
            val model: MainViewModel = koinViewModel()
            model.prefetchData(scope)
            val isOnline by model.isOnline.collectAsState(initial = true)
            OpenUnitoTheme {
                OpenUnitoApp(
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

// figures out the "level" of the current destination
// used to determine animation type
private fun NavBackStackEntry.getTabOrdinal(): Int {
    return AppDestinations.entries.firstOrNull { dest ->
        destination.hierarchy.any { it.hasRoute(dest.graphClass) }
    }?.ordinal ?: -1
}

@Composable
fun OpenUnitoApp(
    isOnline: Boolean = true,
    onSessionExpired: () -> Unit = {},
    navState: OpenUnitoNavState = rememberOpenUnitoNavState()
) {
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = navState.isBackEnabled,
        onBackCompleted = { navState.handleBack() }
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { dest ->
                item(
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.label
                        )
                    },
                    label = { Text(dest.label) },
                    selected = dest == navState.activeTab,
                    onClick = { navState.navigateToTab(dest) }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Cool asf ngl
                NavHost(
                    navController = navState.navController,
                    startDestination = HomeGraph,
                    modifier = Modifier.weight(1f),
                    // thanks to Gemini for the transitions <3
                    enterTransition = { tabAwareEnterTransition() },
                    exitTransition = { tabAwareExitTransition() },
                    popEnterTransition = { tabAwareEnterTransition() },
                    popExitTransition = { tabAwareExitTransition() }
                ) {
                    // Isolated Navigation Subgraph (AWESOME)
                    // (navController is a beast)
                    navigation<HomeGraph>(startDestination = HomeScreenRoute) {
                        composable<HomeScreenRoute> {
                            HomeScreen(
                                onNavigateToSchedule = { navState.navigateToTab(AppDestinations.CALENDAR) },
                                onOpenProfile = { navState.navController.navigate(ProfileScreenRoute) }
                            )
                        }

                        composable<ProfileScreenRoute> {
                            ProfileScreen(
                                onBack = { navState.navController.popBackStack() }
                            )
                        }
                    }

                    navigation<CareerGraph>(startDestination = CareerRoute) {
                        composable<CareerRoute> {
                            CareerScreen(
                                onOpenTranscript = {
                                    navState.navController.navigate(
                                        TranscriptRoute
                                    )
                                },
                                onOpenInvokes = {
                                    navState.navController.navigate(
                                        InvokeRoute
                                    )
                                }
                            )
                        }
                        composable<TranscriptRoute> {
                            TranscriptScreen(onBack = { navState.navController.popBackStack() })
                        }
                        composable<InvokeRoute> {
                            ExamSessionScreen(onBack = { navState.navController.popBackStack() })
                        }
                    }

                    composable<CalendarRoute> {
                        AgendaScreen()
                    }

                    navigation<SettingsGraph>(startDestination = SettingsRoute) {
                        composable<SettingsRoute> {
                            SettingsScreen(
                                onClickAppearance = {
                                    navState.navController.navigate(
                                        AppearenceRoute
                                    )
                                },
                                onClickLanguage = {
                                    navState.navController.navigate(
                                        LanguageRoute
                                    )
                                },
                                onClickDev = {
                                    navState.navController.navigate(
                                        DevRoute
                                    )
                                },
                                onBack = {
                                    navState.navController.popBackStack()
                                },
                            )
                        }
                        composable<AppearenceRoute> {
                            AppearanceScreen(onBack = { navState.navController.popBackStack() })
                        }
                        composable<LanguageRoute> {
                            LanguageScreen(onBack = { navState.navController.popBackStack() })
                        }
                        composable<DevRoute> {
                            DevScreen(onBack = { navState.navController.popBackStack() })
                        }
                    }
                }

                OfflineBanner(isOnline = isOnline)
            }
        }
    }
}

enum class AppDestinations(
    private val labelRes: StringResource,
    val icon: ImageVector,
    val route: Any,
    val graphClass: KClass<*>,
    val rootRouteClass: KClass<*>
) {
    HOME(
        Res.string.home_tab_title,
        Icons.Default.Home,
        HomeGraph,
        HomeGraph::class,
        HomeScreenRoute::class
    ),
    CAREER(
        Res.string.career_tab_title,
        Icons.Default.AccountBox,
        CareerRoute,
        CareerGraph::class,
        CareerRoute::class
    ),
    CALENDAR(
        Res.string.calendar_tab_title,
        Icons.Default.CalendarToday,
        CalendarRoute,
        CalendarRoute::class,
        CalendarRoute::class
    ),
    SETTINGS(
        Res.string.settings_tab_title,
        Icons.Default.Settings,
        SettingsRoute,
        SettingsGraph::class,
        SettingsRoute::class
    );

    val label: String
        @Composable
        get() = stringResource(labelRes)
}

// --- Graphs and Routes definitions ---

// <Home>
// Example of Route that needs a nested graph, remember to update AppDestination's graphClass
@Serializable
private object HomeGraph

@Serializable
private object HomeScreenRoute

@Serializable
private object ProfileScreenRoute
// </Home>

// <Career>
@Serializable
private object CareerGraph

@Serializable
private object CareerRoute
@Serializable
private object TranscriptRoute

@Serializable
private object InvokeRoute
// </Career>

// <Calendar>
@Serializable
private object CalendarRoute
// </Calendar>

// <Settings>
@Serializable
private object SettingsGraph


@Serializable
private object SettingsRoute

@Serializable
private object AppearenceRoute

@Serializable
private object LanguageRoute

@Serializable
private object DevRoute

// </Settings>

/*
Notes:

# Pass data to views
- use data class instead of object
- follow this example:
composable<ProfileScreenRoute> { backStackEntry ->
    // Extract using .toRoute<T>()
    val profileArgs = backStackEntry.toRoute<ProfileScreenRoute>()

    ProfileScreenContent(
        userId = profileArgs.userId,
        onBack = { navController.popBackStack() }
    )
}
 */
