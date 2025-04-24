package com.danioliveira.taskmanager

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.danioliveira.taskmanager.navigation.AppNavigationProvider
import com.danioliveira.taskmanager.navigation.LocalNavController
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.rememberNavigationActions
import com.danioliveira.taskmanager.ui.login.LoginScreen
import com.danioliveira.taskmanager.ui.register.RegisterScreen
import com.danioliveira.taskmanager.ui.task.TaskScreen
import com.danioliveira.taskmanager.ui.tasks.TasksScreen
import com.danioliveira.taskmanager.ui.tasks.TasksViewModel
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// Icons for bottom navigation
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Tasks : BottomNavItem(Screen.Tasks.route, "Tasks", Icons.Default.List)
    object CreateTask : BottomNavItem(Screen.CreateTask.route, "Create Task", Icons.Default.Add)
    object Profile : BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
}

@Composable
@Preview
fun App() {
    TaskItTheme {
        AppNavigationProvider {
            val navController = LocalNavController.current
            val navActions = rememberNavigationActions()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    if (shouldShowBottomBar(currentRoute)) {
                        BottomNavigation {
                            val items = listOf(
                                BottomNavItem.Tasks,
                                BottomNavItem.CreateTask,
                                BottomNavItem.Profile
                            )

                            items.forEach { item ->
                                BottomNavigationItem(
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    label = { Text(item.title) },
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            when (item) {
                                                BottomNavItem.Tasks -> navActions.navigateToTasks()
                                                BottomNavItem.CreateTask -> navActions.navigateToCreateTask()
                                                BottomNavItem.Profile -> navActions.navigateToProfile()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen()
                    }

                    composable(Screen.Register.route) {
                        RegisterScreen()
                    }

                    composable(Screen.Tasks.route) {
                        TasksScreen(TasksViewModel())
                    }

                    composable(
                        route = Screen.Task.route,
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")
                        TaskScreen(
                            taskId = taskId,
                            isCreating = false,
                            onBack = navActions.navigateBack,
                            onTaskUpdated = navActions.navigateBack,
                            onTaskDeleted = navActions.navigateBack
                        )
                    }

                    composable(Screen.CreateTask.route) {
                        TaskScreen(
                            isCreating = true,
                            onBack = navActions.navigateBack,
                            onTaskCreated = navActions.navigateToTasks
                        )
                    }

                    composable(Screen.Profile.route) {
                        // Profile screen will be implemented later
                        Text("Profile Screen - Coming Soon")
                    }
                }
            }
        }
    }
}

private fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return currentRoute == Screen.Tasks.route ||
            currentRoute == Screen.CreateTask.route ||
            currentRoute == Screen.Profile.route
}
