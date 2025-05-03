package com.danioliveira.taskmanager

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import com.danioliveira.taskmanager.navigation.*
import com.danioliveira.taskmanager.ui.login.LoginScreen
import com.danioliveira.taskmanager.ui.projects.ProjectDetailsScreen
import com.danioliveira.taskmanager.ui.projects.ProjectsScreen
import com.danioliveira.taskmanager.ui.register.RegisterScreen
import com.danioliveira.taskmanager.ui.task.TaskScreen
import com.danioliveira.taskmanager.ui.task.TasksCommentsScreen
import com.danioliveira.taskmanager.ui.task.TasksDetailsScreen
import com.danioliveira.taskmanager.ui.task.TasksFilesScreen
import com.danioliveira.taskmanager.ui.tasks.TasksScreen
import com.danioliveira.taskmanager.ui.tasks.TasksViewModel
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.ic_folder
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// Icons for bottom navigation
sealed class NavIcon {
    data class ImageVectorIcon(val imageVector: ImageVector) : NavIcon()
    data class DrawableResourceIcon(val drawableResource: DrawableResource) : NavIcon()
}

sealed class BottomNavItem(val route: String, val title: String, val icon: NavIcon) {
    object Tasks : BottomNavItem(Screen.Tasks.route, "Tasks", NavIcon.ImageVectorIcon(Icons.Default.Check))
    object Projects :
        BottomNavItem(Screen.Projects.route, "Projects", NavIcon.DrawableResourceIcon(Res.drawable.ic_folder))

    object Profile : BottomNavItem(Screen.Profile.route, "Profile", NavIcon.ImageVectorIcon(Icons.Default.Person))
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
                                BottomNavItem.Projects,
                                BottomNavItem.Profile
                            )

                            items.forEach { item ->
                                BottomNavigationItem(
                                    icon = {
                                        when (val icon = item.icon) {
                                            is NavIcon.ImageVectorIcon -> Icon(
                                                imageVector = icon.imageVector,
                                                contentDescription = item.title
                                            )

                                            is NavIcon.DrawableResourceIcon -> Icon(
                                                painter = painterResource(icon.drawableResource),
                                                contentDescription = item.title
                                            )
                                        }
                                    },
                                    label = { Text(item.title) },
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            when (item) {
                                                BottomNavItem.Tasks -> navActions.navigateToTasks()
                                                BottomNavItem.Projects -> navActions.navigateToProjects()
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
                AppNavGraph(
                    navController = navController,
                    startDestination = Screen.Login.route,
                    onLoginScreen = {
                        LoginScreen()
                    },
                    onRegisterScreen = {
                        RegisterScreen()
                    },
                    onTasksScreen = {
                        TasksScreen(TasksViewModel())
                    },
                    onProjectsScreen = {
                        ProjectsScreen()
                    },
                    onProfileScreen = {
                        // Profile screen will be implemented later
                        Text("Profile Screen - Coming Soon")
                    },
                    onTaskScreen = { taskId ->
                        TaskScreen(
                            taskId = taskId,
                            isCreating = false,
                            onBack = navActions.navigateBack,
                            onTaskUpdated = navActions.navigateBack,
                            onTaskDeleted = navActions.navigateBack
                        )
                    },
                    onCreateTaskScreen = {
                        TaskScreen(
                            isCreating = true,
                            onBack = navActions.navigateBack,
                            onTaskCreated = navActions.navigateToTasks
                        )
                    },
                    onTaskDetailsScreen = { taskId ->
                        TasksDetailsScreen(
                            taskId = taskId,
                            onBack = navActions.navigateBack,
                            onFilesClick = navActions.navigateToTaskFiles,
                            onCommentsClick = navActions.navigateToTaskComments
                        )
                    },
                    onTaskFilesScreen = { taskId ->
                        TasksFilesScreen(
                            taskId = taskId,
                            onBack = navActions.navigateBack
                        )
                    },
                    onTaskCommentsScreen = { taskId ->
                        TasksCommentsScreen(
                            taskId = taskId,
                            onBack = navActions.navigateBack
                        )
                    },
                    onProjectDetailsScreen = { projectId ->
                        ProjectDetailsScreen(
                            projectId = projectId,
                            onBack = navActions.navigateBack
                        )
                    }
                )
            }
        }
    }
}

private fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return currentRoute == Screen.Tasks.route ||
            currentRoute == Screen.Projects.route ||
            currentRoute == Screen.Profile.route
}
