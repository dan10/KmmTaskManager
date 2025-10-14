package com.danioliveira.taskmanager.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.danioliveira.taskmanager.core.domain.manager.AuthManager
import com.danioliveira.taskmanager.feature.auth.navigation.LoginRoute
import com.danioliveira.taskmanager.feature.auth.navigation.RegisterRoute
import com.danioliveira.taskmanager.feature.projects.navigation.ProjectsBaseRoute
import com.danioliveira.taskmanager.feature.projects.navigation.ProjectsRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.TasksBaseRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.TasksRoute
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.TopLevelRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * App state holder for the TasksIt application.
 * Handles navigation state and authentication logic.
 * 
 * **Automatic Logout Flow:**
 * 1. 401 error occurs (e.g., token expired, invalid token)
 * 2. KtorClient retries request up to 3 times
 * 3. After 3 failed attempts, KtorClient calls authManager.logout()
 * 4. AuthManager updates isAuthenticated flow to false
 * 5. This class detects the state change and automatically navigates to login
 * 6. Back stack is cleared to prevent returning to authenticated screens
 */
@Stable
class TasksItAppState(
    val navController: NavHostController,
    private val authManager: AuthManager,
    private val coroutineScope: CoroutineScope,
) {

    private val previousDestination = mutableStateOf<NavDestination?>(null)
    private var previousAuthState by mutableStateOf<Boolean?>(null)

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery = _globalSearchQuery.asStateFlow()

    var isSearchVisible by mutableStateOf(false)
        private set

    var userInitials by mutableStateOf<String?>(null)
        private set

    var showBottomBar by mutableStateOf(false)
        private set


    init {
        // Listen to navigation changes for bottom bar visibility
        coroutineScope.launch {
            navController.currentBackStackEntryFlow.collectLatest { currentEntry ->
                currentEntry.destination.also { destination ->
                    showBottomBar = shouldShowBottomBar2(destination)
                }
            }
        }
        
        // Listen to authentication state changes for automatic logout navigation
        // This ensures the app automatically redirects to login when user is logged out
        // (e.g., from 401 error after retries, manual logout, etc.)
        coroutineScope.launch {
            authManager.isAuthenticated.collectLatest { isAuthenticated ->
                // Only handle logout navigation (authenticated -> not authenticated)
                if (previousAuthState == true && !isAuthenticated) {
                    handleLogout()
                }
                previousAuthState = isAuthenticated
            }
        }

        coroutineScope.launch {
            val user = authManager.getCurrentUser()
            userInitials = user?.displayName?.takeIf { it.isNotBlank() }
                ?.let { computeInitials(it) }
                ?: user?.email?.let { computeInitials(it) }
        }

        coroutineScope.launch {
            navController.currentBackStackEntryFlow.collectLatest { currentEntry ->
                val destination = currentEntry.destination
                if (!shouldShowToolbar(destination)) {
                    isSearchVisible = false
                    _globalSearchQuery.value = ""
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
        return currentDestination.hierarchy.any {
            it.hasRoute(LoginRoute::class) ||
            it.hasRoute(RegisterRoute::class)
        }
    }


    private fun shouldShowBottomBar2(currentDestination: NavDestination): Boolean {
        return currentDestination.hierarchy.any {
            it.hasRoute(TasksRoute::class) ||
            it.hasRoute(ProjectsRoute::class) ||
            it.hasRoute(Screen.Calendar::class) ||
            it.hasRoute(Screen.Profile::class)
        }
    }

    /**
     * Handles logout by navigating to login screen and clearing the back stack.
     */
    private fun handleLogout() {
        // Check if we're not already on login/register screen
        val currentDest = navController.currentDestination
        if (currentDest != null && !isLoginOrRegisterScreen(currentDest)) {
            navController.navigate(LoginRoute) {
                // Clear entire back stack
                popUpTo(0) {
                    inclusive = true
                }
                // Single instance of login screen
                launchSingleTop = true
            }
        }
    }

    /**
     * Handles navigation based on authentication state.
     */
    fun handleAuthNavigation() {
        coroutineScope.launch {
            if (isAuthenticated.value) {
                // If authenticated, navigate to home
                navController.navigate(TasksBaseRoute) {
                    popUpTo(Screen.Login) {
                        inclusive = true
                    }
                }
            } else {
                // If not authenticated, navigate to login
                handleLogout()
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

    fun navigateToTasks() {
        navController.navigate(TasksBaseRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToProjects() {
        navController.navigate(ProjectsBaseRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToProfile() {
        navController.navigate(Screen.Profile) {
            launchSingleTop = true
        }
    }

    fun toggleSearchVisibility() {
        isSearchVisible = !isSearchVisible
        if (!isSearchVisible) {
            _globalSearchQuery.value = ""
        }
    }

    fun onGlobalSearchQueryChanged(query: String) {
        _globalSearchQuery.value = query
    }

    fun shouldShowToolbar(destination: NavDestination): Boolean {
        return destination.hierarchy.any {
            it.hasRoute(TasksRoute::class) ||
            it.hasRoute(Screen.Calendar::class) ||
            it.hasRoute(ProjectsRoute::class)
        }
    }

    fun isToolbarDestination(destination: NavDestination?): Boolean =
        destination?.let { shouldShowToolbar(it) } ?: false

    private fun computeInitials(source: String): String {
        val trimmed = source.trim()
        if (trimmed.isEmpty()) return ""
        val parts = trimmed.split(" ").filter { it.isNotBlank() }
        val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar()
        val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
        return buildString {
            first?.let { append(it) }
            second?.let { append(it) }
        }.ifEmpty { trimmed.first().uppercaseChar().toString() }
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

