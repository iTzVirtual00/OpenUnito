package me.itzvirtual.openunito.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import me.itzvirtual.openunito.MainViewModel
import me.itzvirtual.openunito.resources.*
import me.itzvirtual.openunito.ui.feature_agenda.AgendaScreen
import me.itzvirtual.openunito.ui.feature_career.CareerScreen
import me.itzvirtual.openunito.ui.feature_devtools.DevtoolsScreen
import me.itzvirtual.openunito.ui.feature_examsessions.InvokeScreen
import me.itzvirtual.openunito.ui.feature_home.HomeScreen
import me.itzvirtual.openunito.ui.feature_login.AuthViewModel
import me.itzvirtual.openunito.ui.feature_login.UniToLoginScreen
import me.itzvirtual.openunito.ui.feature_profile.ProfileScreen
import me.itzvirtual.openunito.ui.shared.OfflineBanner
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass


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
) {
//    Log.d("OpenUnito", "Current locale: ${LocalConfiguration.current.locales[0].language}")

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination // this also holds sub-views

    // Least Recently Used back history for root components only
    val tabHistory = remember { mutableStateListOf(AppDestinations.HOME) }

    // Use hasRoute to determine the owner of currentDestination (magic!)
    val activeTab = AppDestinations.entries.firstOrNull { dest ->
        currentDestination?.hierarchy?.any { it.hasRoute(dest.graphClass) } == true
    } ?: tabHistory.last()
    // Are any nested screens open?
    val isCurrentTabAtRoot = currentDestination?.hasRoute(activeTab.rootRouteClass) == true

    // see BackHandler comments for more info
    val canPopHistory = tabHistory.size > 1
    val shouldFallbackToHome = tabHistory.size <= 1 && activeTab != AppDestinations.HOME

    // TODO: move this mf outta here
    fun navigateToTab(targetTab: AppDestinations, isBackNav: Boolean = false) {
        if (targetTab == activeTab && isCurrentTabAtRoot && !isBackNav) return

        navController.navigate(targetTab.route) {
            // Pop the currently active tab graph and snapshot its state
            popUpTo(activeTab.route) { // No idea why popUpTo works here
                saveState = true
                inclusive = true
            }
            // Restores the state
            restoreState = true
            launchSingleTop = true
        }

        if (!isBackNav) {
            tabHistory.remove(targetTab)
            tabHistory.add(targetTab)
        }
    }

    // Only step through the LRU tabHistory if:
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = isCurrentTabAtRoot && (canPopHistory || shouldFallbackToHome),
        onBackCompleted = {
            if (canPopHistory) {
                // Normal LRU pop
                tabHistory.removeAt(tabHistory.lastIndex)
                val previousTab = tabHistory.last()
                navigateToTab(previousTab, isBackNav = true)
            } else {
                // Fallback to Home if we are not at home and stack is empty (more natural)
                tabHistory.clear()
                tabHistory.add(AppDestinations.HOME)
                navigateToTab(AppDestinations.HOME, isBackNav = true)
            }
        }
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { dest ->
                val isSelected = dest == activeTab

                item(
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.label
                        )
                    },
                    label = { Text(dest.label) },
                    selected = isSelected,
                    onClick = { navigateToTab(dest) }
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
                    navController = navController,
                    startDestination = HomeGraph,
                    modifier = Modifier.weight(1f),
                    // thanks to Gemini for the transitions <3
                    enterTransition = {
                        val initialTab = initialState.getTabOrdinal()
                        val targetTab = targetState.getTabOrdinal()

                        if (initialTab != targetTab && initialTab != -1 && targetTab != -1) {
                            // Tab switch: slide based on bottom bar order
                            if (targetTab > initialTab) {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            } else {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            }
                        } else {
                            // Nested navigation push (e.g., Home -> Profile)
                            fadeIn(animationSpec = tween(300))
                        }
                    },
                    exitTransition = {
                        val initialTab = initialState.getTabOrdinal()
                        val targetTab = targetState.getTabOrdinal()

                        if (initialTab != targetTab && initialTab != -1 && targetTab != -1) {
                            // Tab switch exit
                            if (targetTab > initialTab) {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            } else {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        } else {
                            // Nested navigation exit
                            fadeOut(animationSpec = tween(300))
                        }
                    },
                    popEnterTransition = {
                        val initialTab = initialState.getTabOrdinal()
                        val targetTab = targetState.getTabOrdinal()

                        if (initialTab != targetTab && initialTab != -1 && targetTab != -1) {
                            // Tab switch triggered by back press
                            if (targetTab > initialTab) {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            } else {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                                ) + fadeIn(animationSpec = tween(300))
                            }
                        } else {
                            // Nested pop back (e.g., Profile -> Home)
                            fadeIn(animationSpec = tween(300))
                        }
                    },
                    popExitTransition = {
                        val initialTab = initialState.getTabOrdinal()
                        val targetTab = targetState.getTabOrdinal()

                        if (initialTab != targetTab && initialTab != -1 && targetTab != -1) {
                            // Tab switch exit triggered by back press
                            if (targetTab > initialTab) {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            } else {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(300)
                                ) + fadeOut(animationSpec = tween(300))
                            }
                        } else {
                            // Nested pop back exit
                            fadeOut(animationSpec = tween(300))
                        }
                    }
                ) {
                    // Isolated Navigation Subgraph (AWESOME)
                    // (navController is a beast)
                    navigation<HomeGraph>(startDestination = HomeScreenRoute) {
                        composable<HomeScreenRoute> {
                            HomeScreen(
                                onNavigateToSchedule = { navigateToTab(AppDestinations.CALENDAR) },
                                onOpenProfile = { navController.navigate(ProfileScreenRoute) }
                            )
                        }

                        composable<ProfileScreenRoute> {
                            ProfileScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    navigation<CareerGraph>(startDestination = CareerRoute){
                        composable<CareerRoute> {
                            CareerScreen(onOpenInvokes = { navController.navigate(InvokeRoute) })
                        }
                        composable<InvokeRoute> {
                            InvokeScreen()
                        }
                    }

                    composable<CalendarRoute> {
                        AgendaScreen()
                    }

                    composable<DevToolsRoute> {
                        DevtoolsScreen()
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
    DEVTOOLS(
        Res.string.dev_tab_title,
        Icons.Default.LogoDev,
        DevToolsRoute,
        DevToolsRoute::class,
        DevToolsRoute::class
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
private object InvokeRoute
// </Career>

// <Calendar>
@Serializable
private object CalendarRoute
// </Calendar>

// <DevTools>
@Serializable
private object DevToolsRoute
// </DevTools>

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
