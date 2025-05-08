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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.danioliveira.taskmanager.domain.manager.AuthManager
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.TopLevelRoute
import kotlinx.coroutines.CoroutineScope
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
    val isAuthenticated: Boolean,
    val currentDestination: NavDestination?
) {
    var isInitialAuthCheckDone by mutableStateOf(false)
        private set

    /**
     * Checks if the current screen is a login or register screen.
     */
    fun isLoginOrRegisterScreen(): Boolean {
        return currentDestination?.hasRoute(Screen.Login::class) == true ||
                currentDestination?.hasRoute(Screen.Register::class) == true
    }

    /**
     * Checks if the bottom bar should be shown for the current screen.
     */
    fun shouldShowBottomBar(): Boolean {
        return currentDestination?.hasRoute(Screen.Tasks::class) == true ||
                currentDestination?.hasRoute(Screen.Projects::class) == true ||
                currentDestination?.hasRoute(Screen.Profile::class) == true
    }

    /**
     * Performs the initial authentication check when the app starts.
     */
    fun checkAuthState() {
        coroutineScope.launch {
            authManager.checkAuthState()
            isInitialAuthCheckDone = true
        }
    }

    /**
     * Handles navigation based on authentication state.
     */
    fun handleAuthNavigation() {
        if (!isInitialAuthCheckDone) return

        if (isAuthenticated) {
            // If authenticated, navigate to home
            navController.navigate(Screen.Tasks) {
                popUpTo(Screen.Login) {
                    inclusive = true
                }
            }
        } else {
            // If not authenticated, navigate to login
            // Check if current route is not login or register
            if (currentDestination?.route != null && !isLoginOrRegisterScreen()) {
                navController.navigate(Screen.Login) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
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
    val isAuthenticated by authManager.isAuthenticated.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    return remember(navController, authManager, coroutineScope, isAuthenticated, currentDestination) {
        TasksItAppState(
            navController = navController,
            authManager = authManager,
            coroutineScope = coroutineScope,
            isAuthenticated = isAuthenticated,
            currentDestination = currentDestination
        )
    }
}
