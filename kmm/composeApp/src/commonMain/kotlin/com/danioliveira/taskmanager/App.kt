package com.danioliveira.taskmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danioliveira.taskmanager.domain.manager.AuthManager
import com.danioliveira.taskmanager.navigation.NavIcon
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.topLevelRoutes
import com.danioliveira.taskmanager.ui.login.LoginScreen
import com.danioliveira.taskmanager.ui.project.create.CreateEditProjectScreen
import com.danioliveira.taskmanager.ui.project.details.ProjectDetailsScreen
import com.danioliveira.taskmanager.ui.projects.ProjectsScreen
import com.danioliveira.taskmanager.ui.register.RegisterScreen
import com.danioliveira.taskmanager.ui.task.create.TaskCreateEditScreen
import com.danioliveira.taskmanager.ui.task.details.TasksDetailsScreen
import com.danioliveira.taskmanager.ui.tasks.TasksScreen
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun TaskItApp(
    onAppReady: () -> Unit = {}
) {
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
                onAppReady = onAppReady,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
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
        NavigationBar(modifier = Modifier.navigationBarsPadding()) {
            topLevelRoutes.forEach { topLevelRoute ->
                NavigationBarItem(
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
                    selected = appState.currentDestination?.hierarchy?.any {
                        it.hasRoute(
                            topLevelRoute.route::class
                        )
                    } == true,
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
    modifier: Modifier = Modifier,
    onAppReady: () -> Unit = {},
) {
    val authManager = koinInject<AuthManager>()
    var startDestination by remember { mutableStateOf<Screen?>(null) }
    
    // Check authentication state on startup
    LaunchedEffect(Unit) {
        val isAuthenticated = authManager.checkAuthState()
        startDestination = if (isAuthenticated) Screen.Tasks else Screen.Login
        // Signal that the app is ready (hide native splash screen)
        onAppReady()
    }
    
    // Show nothing until we determine the start destination
    startDestination?.let { destination ->
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = destination
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
                navigateToCreateProject = {
                    navController.navigate(Screen.CreateEditProject(null))
                },
                navigateToProjectDetail = { projectId ->
                    navController.navigate(
                        Screen.ProjectDetails(
                            projectId
                        )
                    )
                }
            )
        }

        composable<Screen.Profile> {
            Text("Profile Screen - Coming Soon")
        }

        composable<Screen.CreateEditProject> { backStackEntry ->
            CreateEditProjectScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.CreateEditTask> {
            TaskCreateEditScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable<Screen.TasksDetails> { backStackEntry ->
            TasksDetailsScreen(
                onBack = { navController.popBackStack() },
                onEditTask = { taskId ->
                    navController.navigate(Screen.CreateEditTask(taskId))
                }
            )
        }

        composable<Screen.ProjectDetails> { backStackEntry ->
            ProjectDetailsScreen(
                onBack = { navController.popBackStack() },
                navigateToCreateTask = {
                    navController.navigate(Screen.CreateEditTask(taskId = null, projectId = it))
                },
                navigateToTaskDetail = { taskId ->
                    navController.navigate(Screen.TasksDetails(taskId.toString()))
                }
            )
        }
        }
    }
}
