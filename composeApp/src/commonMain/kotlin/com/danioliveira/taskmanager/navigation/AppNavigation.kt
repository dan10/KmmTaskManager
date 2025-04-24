package com.danioliveira.taskmanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// Create a CompositionLocal for the NavController
val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController provided") }

// Screens in the app
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Tasks : Screen("tasks")
    object Task : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }

    object CreateTask : Screen("create_task")
    object Profile : Screen("profile")
}

// Navigation actions
class NavigationActions(private val navController: NavHostController) {
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    val navigateToRegister: () -> Unit = {
        navController.navigate(Screen.Register.route)
    }

    val navigateToTasks: () -> Unit = {
        navController.navigate(Screen.Tasks.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    val navigateToCreateTask: () -> Unit = {
        navController.navigate(Screen.CreateTask.route)
    }

    val navigateToTask: (String) -> Unit = { taskId ->
        navController.navigate(Screen.Task.createRoute(taskId))
    }

    val navigateToProfile: () -> Unit = {
        navController.navigate(Screen.Profile.route)
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

// Provider for the NavController
@Composable
fun AppNavigationProvider(content: @Composable () -> Unit) {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        content()
    }
}

// Helper to get the NavController
@Composable
fun rememberNavigationActions(): NavigationActions {
    val navController = LocalNavController.current
    return remember(navController) { NavigationActions(navController) }
}