package com.danioliveira.taskmanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.danioliveira.taskmanager.domain.manager.AuthManager
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.TopLevelRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App state holder for the TasksIt application.
 * Handles navigation state and authentication logic.
 */
@Stable
class TasksItAppState(
    val navController: NavHostController,
    private val authManager: AuthManager,
    private val coroutineScope: CoroutineScope,
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)

    var showBottomBar by mutableStateOf(false)
        private set


    init {
        coroutineScope.launch {
            authManager.checkAuthState()
            handleAuthNavigation()
        }

        coroutineScope.launch {
            navController.currentBackStackEntryFlow.collectLatest { currentEntry ->
                currentEntry.destination.also { destination ->
                    showBottomBar = shouldShowBottomBar2(destination)
                }
            }
        }
    }

    val currentDestination: NavDestination?
        @Composable get() {
            // Collect the currentBackStackEntryFlow as a state
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            // Fallback to previousDestination if currentEntry is null
            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val isAuthenticated = authManager.isAuthenticated.stateIn(
        started = SharingStarted.Eagerly,
        initialValue = false,
        scope = coroutineScope
    )

    /**
     * Checks if the current screen is a login or register screen.
     */
    fun isLoginOrRegisterScreen(currentDestination: NavDestination): Boolean {
        return currentDestination.hasRoute(Screen.Login::class) ||
                currentDestination.hasRoute(Screen.Register::class)
    }

    /**
     * Checks if the bottom bar should be shown for the current screen.
     */
    @Composable
    fun shouldShowBottomBar(): Boolean {
        return currentDestination?.hasRoute(Screen.Tasks::class) == true ||
                currentDestination?.hasRoute(Screen.Projects::class) == true ||
                currentDestination?.hasRoute(Screen.Profile::class) == true
    }

    private fun shouldShowBottomBar2(currentDestination: NavDestination): Boolean {
        return currentDestination.hasRoute(Screen.Tasks::class) ||
                currentDestination.hasRoute(Screen.Projects::class) ||
                currentDestination.hasRoute(Screen.Profile::class)
    }

    /**
     * Handles navigation based on authentication state.
     */

    fun handleAuthNavigation() {
        coroutineScope.launch {
            if (isAuthenticated.value) {
                // If authenticated, navigate to home
                navController.navigate(Screen.Tasks) {
                    popUpTo(Screen.Login) {
                        inclusive = true
                    }
                }
            } else {
                // If not authenticated, navigate to login
                // Check if current route is not login or register
                if (navController.currentDestination?.route != null &&
                    !isLoginOrRegisterScreen(navController.currentDestination!!)
                ) {
                    navController.navigate(Screen.Login) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    /**
     * Navigates to a top-level destination.
     */
    fun navigateToTopLevelDestination(topLevelRoute: TopLevelRoute<*>) {
        navController.navigate(topLevelRoute.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

/**
 * Creates and remembers a TasksItAppState.
 */
@Composable
fun rememberTasksItAppState(
    navController: NavHostController = rememberNavController(),
    authManager: AuthManager,
    coroutineScope: CoroutineScope
): TasksItAppState {
    return remember(navController, authManager, authManager.isAuthenticated, coroutineScope) {
        TasksItAppState(
            navController = navController,
            authManager = authManager,
            coroutineScope = coroutineScope,
        )
    }
}
