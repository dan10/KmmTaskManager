package com.danioliveira.taskmanager

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.navigation.BottomNavItem
import com.danioliveira.taskmanager.navigation.NavIcon
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.ui.login.LoginScreen
import com.danioliveira.taskmanager.ui.projects.ProjectDetailsScreen
import com.danioliveira.taskmanager.ui.projects.ProjectsScreen
import com.danioliveira.taskmanager.ui.register.RegisterScreen
import com.danioliveira.taskmanager.ui.task.TaskCreatEditScreen
import com.danioliveira.taskmanager.ui.task.TasksCommentsScreen
import com.danioliveira.taskmanager.ui.task.TasksDetailsScreen
import com.danioliveira.taskmanager.ui.task.TasksFilesScreen
import com.danioliveira.taskmanager.ui.tasks.TasksScreen
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun TaskItApp() {
    TaskItTheme {
        val navController = rememberNavController()
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStackEntry?.destination


        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar(currentDestination?.route)) {
                    BottomNavigation {
                        val items = listOf(
                            BottomNavItem.Tasks,
                            BottomNavItem.Projects,
                            BottomNavItem.Profile
                        )

                        items.forEach { item ->
                            BottomNavigationItem(
                                icon = {
                                    when (val icon = item.icon) {
                                        is NavIcon.ImageVectorIcon -> Icon(
                                            imageVector = icon.imageVector,
                                            contentDescription = stringResource(item.title)
                                        )

                                        is NavIcon.DrawableResourceIcon -> Icon(
                                            painter = painterResource(icon.drawableResource),
                                            contentDescription = stringResource(item.title)
                                        )
                                    }
                                },
                                label = { Text(stringResource(item.title)) },
                                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) {
            TaskItNavHost(
                navController = navController
            )
        }
    }
}

private fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return currentRoute == BottomNavItem.Tasks.route ||
            currentRoute == BottomNavItem.Projects.route ||
            currentRoute == BottomNavItem.Profile.route
}

@Composable
fun TaskItNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login
    ) {
        // Authentication
        composable<Screen.Login> {
            LoginScreen()
        }

        composable<Screen.Register> {
            RegisterScreen(navigateToLogin = {
                navController.popBackStack()
            })
        }

        // Top level destinations
        composable(BottomNavItem.Tasks.route) {
            TasksScreen()
        }

        composable(BottomNavItem.Projects.route) {
            ProjectsScreen()
        }

        composable(BottomNavItem.Profile.route) {
            Text("Profile Screen - Coming Soon")
        }

        composable<Screen.CreateEditTask> { backStackEntry ->
            val task = backStackEntry.toRoute<Screen.CreateEditTask>()
            val taskId = task.taskId
            TaskCreatEditScreen(
                taskId = taskId,
                isCreating = false,
                onBack = { navController.popBackStack() },
                onTaskUpdated = { navController.popBackStack() },
                onTaskDeleted = { navController.popBackStack() }
            )
        }

        composable<Screen.TasksDetails> { backStackEntry ->
            val taskId = backStackEntry.toRoute<Screen.TasksDetails>().taskId
            TasksDetailsScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onFilesClick = { navController.navigate(Screen.TasksFiles(taskId)) },
                onCommentsClick = { navController.navigate(Screen.TasksComments(taskId)) }
            )
        }

        composable<Screen.TasksFiles> { backStackEntry ->
            val taskId = backStackEntry.toRoute<Screen.TasksFiles>().taskId
            TasksFilesScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.TasksComments> { backStackEntry ->
            val taskId = backStackEntry.toRoute<Screen.TasksComments>().taskId
            TasksCommentsScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.ProjectDetails> { backStackEntry ->
            val projectId = backStackEntry.toRoute<Screen.ProjectDetails>().projectId
            ProjectDetailsScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
