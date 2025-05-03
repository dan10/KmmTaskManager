package com.danioliveira.taskmanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Create a CompositionLocal for the NavController
val LocalNavController = compositionLocalOf<NavHostController> { error("No NavController provided") }

// Screens in the app
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    // Top level destinations
    object Tasks : Screen("tasks")
    object Projects : Screen("projects")
    object Profile : Screen("profile")

    // Task related screens
    object TasksDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }

    object TasksFiles : Screen("task_files/{taskId}") {
        fun createRoute(taskId: String) = "task_files/$taskId"
    }

    object TasksComments : Screen("task_comments/{taskId}") {
        fun createRoute(taskId: String) = "task_comments/$taskId"
    }

    // Project related screens
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }

    object CreateTask : Screen("create_task")
    object Task : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }
}

// Navigation actions
class NavigationActions(private val navController: NavHostController) {
    // Authentication
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    val navigateToRegister: () -> Unit = {
        navController.navigate(Screen.Register.route)
    }

    // Top level destinations
    val navigateToTasks: () -> Unit = {
        navController.navigate(Screen.Tasks.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    val navigateToProjects: () -> Unit = {
        navController.navigate(Screen.Projects.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
        }
    }

    val navigateToProfile: () -> Unit = {
        navController.navigate(Screen.Profile.route)
    }

    // Task related navigation
    val navigateToCreateTask: () -> Unit = {
        navController.navigate(Screen.CreateTask.route)
    }

    val navigateToTask: (String) -> Unit = { taskId ->
        navController.navigate(Screen.Task.createRoute(taskId))
    }

    val navigateToTaskDetails: (String) -> Unit = { taskId ->
        navController.navigate(Screen.TasksDetails.createRoute(taskId))
    }

    val navigateToTaskFiles: (String) -> Unit = { taskId ->
        navController.navigate(Screen.TasksFiles.createRoute(taskId))
    }

    val navigateToTaskComments: (String) -> Unit = { taskId ->
        navController.navigate(Screen.TasksComments.createRoute(taskId))
    }

    // Project related navigation
    val navigateToProjectDetails: (String) -> Unit = { projectId ->
        navController.navigate(Screen.ProjectDetails.createRoute(projectId))
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

// Navigation Graph
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    onTasksScreen: @Composable () -> Unit,
    onProjectsScreen: @Composable () -> Unit,
    onProfileScreen: @Composable () -> Unit,
    onLoginScreen: @Composable () -> Unit,
    onRegisterScreen: @Composable () -> Unit,
    onTaskScreen: @Composable (String?) -> Unit,
    onCreateTaskScreen: @Composable () -> Unit,
    onTaskDetailsScreen: @Composable (String?) -> Unit,
    onTaskFilesScreen: @Composable (String?) -> Unit,
    onTaskCommentsScreen: @Composable (String?) -> Unit,
    onProjectDetailsScreen: @Composable (String?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication
        composable(Screen.Login.route) {
            onLoginScreen()
        }

        composable(Screen.Register.route) {
            onRegisterScreen()
        }

        // Top level destinations
        composable(Screen.Tasks.route) {
            onTasksScreen()
        }

        composable(Screen.Projects.route) {
            onProjectsScreen()
        }

        composable(Screen.Profile.route) {
            onProfileScreen()
        }

        // Task related screens
        composable(
            route = Screen.Task.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            onTaskScreen(taskId)
        }

        composable(Screen.CreateTask.route) {
            onCreateTaskScreen()
        }

        composable(
            route = Screen.TasksDetails.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            onTaskDetailsScreen(taskId)
        }

        composable(
            route = Screen.TasksFiles.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            onTaskFilesScreen(taskId)
        }

        composable(
            route = Screen.TasksComments.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            onTaskCommentsScreen(taskId)
        }

        // Project related screens
        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")
            onProjectDetailsScreen(projectId)
        }
    }
}
