package com.danioliveira.taskmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.domain.manager.AuthManager
import com.danioliveira.taskmanager.navigation.NavIcon
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.topLevelRoutes
import com.danioliveira.taskmanager.ui.login.LoginScreen
import com.danioliveira.taskmanager.ui.project.ProjectDetailsScreen
import com.danioliveira.taskmanager.ui.projects.ProjectsScreen
import com.danioliveira.taskmanager.ui.register.RegisterScreen
import com.danioliveira.taskmanager.ui.task.comments.TasksCommentsScreen
import com.danioliveira.taskmanager.ui.task.create.TaskCreateEditScreen
import com.danioliveira.taskmanager.ui.task.details.TasksDetailsScreen
import com.danioliveira.taskmanager.ui.task.files.TaskFilesScreen
import com.danioliveira.taskmanager.ui.tasks.TasksScreen
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun TaskItApp() {
    TaskItTheme {
        val authManager = koinInject<AuthManager>()
        val navController = rememberNavController()
        val appState = rememberTasksItAppState(
            navController = navController,
            authManager = authManager,
            coroutineScope = rememberCoroutineScope()
        )

        Scaffold(
            modifier = Modifier,
            bottomBar = {
                TaskItBottomBar(appState = appState)
            }
        ) { innerPadding ->
            TaskItNavHost(
                navController = appState.navController,
                modifier = Modifier
                    .padding(innerPadding)
                    .statusBarsPadding()
            )
        }
    }
}

/**
 * Bottom navigation bar for the TaskIt app.
 * Shows navigation items for top-level destinations.
 *
 * @param appState The app state that contains navigation and UI state information
 */
@Composable
fun TaskItBottomBar(
    appState: TasksItAppState
) {
    val showBottomBar by remember {
        derivedStateOf {
            appState.showBottomBar
        }
    }

    AnimatedVisibility(
        visible = showBottomBar,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        BottomNavigation(modifier = Modifier.navigationBarsPadding()) {
            topLevelRoutes.forEach { topLevelRoute ->
                BottomNavigationItem(
                    icon = {
                        when (val icon = topLevelRoute.icon) {
                            is NavIcon.ImageVectorIcon -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = stringResource(topLevelRoute.name)
                            )

                            is NavIcon.DrawableResourceIcon -> Icon(
                                painter = painterResource(icon.drawableResource),
                                contentDescription = stringResource(topLevelRoute.name)
                            )
                        }
                    },
                    label = { Text(stringResource(topLevelRoute.name)) },
                    selected = appState.currentDestination?.hierarchy?.any { it.hasRoute(topLevelRoute.route::class) } == true,
                    onClick = {
                        appState.navigateToTopLevelDestination(topLevelRoute)
                    }
                )
            }
        }
    }
}

@Composable
fun TaskItNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Login
    ) {
        // Authentication
        composable<Screen.Login> {
            LoginScreen(
                navigateToRegister = {
                    navController.navigate(Screen.Register)
                },
                navigateToHome = {
                    navController.navigate(Screen.Tasks) {
                        popUpTo(Screen.Login) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                navigateToLogin = {
                    navController.popBackStack()
                },
                navigateToHome = {
                    navController.navigate(Screen.Tasks) {
                        popUpTo(Screen.Login) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Top level destinations
        composable<Screen.Tasks> {
            TasksScreen(
                navigateToTaskDetail = { taskId -> navController.navigate(Screen.TasksDetails(taskId.toString())) },
                navigateToCreateTask = { navController.navigate(Screen.CreateEditTask(null)) }
            )
        }

        composable<Screen.Projects> {
            ProjectsScreen(
                navigateToCreateProject = { navController.navigate(Screen.CreateEditProject(null)) },
                navigateToProjectDetail = { projectId -> navController.navigate(Screen.ProjectDetails(projectId)) }
            )
        }

        composable<Screen.Profile> {
            Text("Profile Screen - Coming Soon")
        }

        composable<Screen.CreateEditProject> {
            Text("Create Project Screen - Coming Soon")
        }

        composable<Screen.CreateEditTask> {
            TaskCreateEditScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.TasksDetails> { backStackEntry ->
            TasksDetailsScreen(
                onBack = { navController.popBackStack() },
                onFilesClick = { id -> navController.navigate(Screen.TasksFiles(id)) }
            )
        }

        composable<Screen.TasksFiles> { backStackEntry ->
            TaskFilesScreen(
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
            ProjectDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
