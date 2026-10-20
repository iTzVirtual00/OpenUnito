package me.itzvirtual.openunito.ui.shared

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.itzvirtual.openunito.ui.AppDestinations

@Composable
fun rememberOpenUnitoNavState(
    navController: NavHostController = rememberNavController(),
): OpenUnitoNavState {
    val currentBackStackEntryState = navController.currentBackStackEntryAsState()

    // can't pass stack entry as dependency as that would reset the whole object
    return remember(navController) {
        OpenUnitoNavState(navController, currentBackStackEntryState)
    }
}
class OpenUnitoNavState(
    val navController: NavHostController,
    private val entryState: State<NavBackStackEntry?>
) {
    // Least Recently Used back history for root components
    val tabHistory = mutableStateListOf(AppDestinations.HOME)

    val activeTab: AppDestinations by derivedStateOf {
        AppDestinations.entries.firstOrNull { d ->
            entryState.value?.destination?.hierarchy?.any { it.hasRoute(d.graphClass) } == true
        } ?: tabHistory.lastOrNull() ?: AppDestinations.HOME
    }

    val isCurrentTabAtRoot: Boolean by derivedStateOf {
        entryState.value?.destination?.hasRoute(activeTab.rootRouteClass) == true
    }

    val isBackEnabled: Boolean by derivedStateOf {
        val canPopHistory = tabHistory.size > 1
        val shouldFallbackToHome = tabHistory.size <= 1 && activeTab != AppDestinations.HOME
        isCurrentTabAtRoot && (canPopHistory || shouldFallbackToHome)
    }

    fun navigateToTab(
        targetTab: AppDestinations,
        isBackNav: Boolean = false
    ) {
        if (targetTab != activeTab) {
            navController.navigate(targetTab.route) {
                // save activeTab's graph before switching to another tab
                popUpTo(activeTab.route) {
                    saveState = true
                    inclusive = true
                }
                restoreState = true
                launchSingleTop = true
            }

            if (!isBackNav) {
                tabHistory.remove(targetTab)
                tabHistory.add(targetTab)
            }
        } else if (!isCurrentTabAtRoot) {
            // close all graph's children if target tab is the same as the current tab
            // targetTab == activeTab && !isCurrentTabAtRoot == false
            navController.popBackStack(
                route = targetTab.rootRouteClass,
                inclusive = false
            )
        }
    }

    fun handleBack() {
        if (tabHistory.size > 1) {
            tabHistory.removeAt(tabHistory.lastIndex)
            val previousTab = tabHistory.last()
            navigateToTab(previousTab, true)
        } else {
            tabHistory.clear()
            tabHistory.add(AppDestinations.HOME)
            navigateToTab(AppDestinations.HOME, true)
        }
    }
}

private fun NavBackStackEntry.getTabOrdinal(): Int {
    return AppDestinations.entries.firstOrNull { dest ->
        destination.hierarchy.any { it.hasRoute(dest.graphClass) }
    }?.ordinal ?: -1
}

// thanks to Gemini for the transitions <3
// Also thanks to Gemini for the tabAware* extensions
fun AnimatedContentTransitionScope<NavBackStackEntry>.tabAwareEnterTransition(): EnterTransition {
    val initialTab = initialState.getTabOrdinal()
    val targetTab = targetState.getTabOrdinal()

    return if (initialTab != targetTab && initialTab != -1 && targetTab != -1) {
        val direction = if (targetTab > initialTab) {
            AnimatedContentTransitionScope.SlideDirection.Start
        } else {
            AnimatedContentTransitionScope.SlideDirection.End
        }
        slideIntoContainer(direction, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
    } else {
        fadeIn(animationSpec = tween(300))
    }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.tabAwareExitTransition(): ExitTransition {
    val initialTab = initialState.getTabOrdinal()
    val targetTab = targetState.getTabOrdinal()

    return if (initialTab != targetTab && initialTab != -1 && targetTab != -1) {
        val direction = if (targetTab > initialTab) {
            AnimatedContentTransitionScope.SlideDirection.Start
        } else {
            AnimatedContentTransitionScope.SlideDirection.End
        }
        slideOutOfContainer(direction, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    } else {
        fadeOut(animationSpec = tween(300))
    }
}