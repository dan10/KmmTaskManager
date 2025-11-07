@file:OptIn(
    ExperimentalSharedTransitionApi::class,
)

package com.danioliveira.taskmanager

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danioliveira.taskmanager.core.domain.manager.AuthManager
import com.danioliveira.taskmanager.core.ui.TasksItAppState
import com.danioliveira.taskmanager.core.ui.components.PrincipalTaskItTopAppBar
import com.danioliveira.taskmanager.core.ui.rememberTasksItAppState
import com.danioliveira.taskmanager.feature.auth.navigation.AuthBaseRoute
import com.danioliveira.taskmanager.feature.auth.navigation.LoginRoute
import com.danioliveira.taskmanager.feature.auth.navigation.RegisterRoute
import com.danioliveira.taskmanager.feature.auth.navigation.authSection
import com.danioliveira.taskmanager.feature.calendar.ui.CalendarScreen
import com.danioliveira.taskmanager.feature.profile.ui.ProfileScreen
import com.danioliveira.taskmanager.feature.projects.navigation.ProjectDetailRoute
import com.danioliveira.taskmanager.feature.projects.navigation.ProjectsRoute
import com.danioliveira.taskmanager.feature.projects.navigation.projectsSection
import com.danioliveira.taskmanager.feature.tasks.navigation.EditTaskRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.TaskDetailRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.TasksBaseRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.TasksRoute
import com.danioliveira.taskmanager.feature.tasks.navigation.taskDetailsScreen
import com.danioliveira.taskmanager.feature.tasks.navigation.tasksSection
import com.danioliveira.taskmanager.navigation.composableWithCompositionLocal
import com.danioliveira.taskmanager.navigation.NavIcon
import com.danioliveira.taskmanager.navigation.Screen
import com.danioliveira.taskmanager.navigation.topLevelRoutes
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
                appState = appState,
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TaskItNavHost(
    navController: NavHostController,
    appState: TasksItAppState,
    modifier: Modifier = Modifier,
    onAppReady: () -> Unit = {}
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
        val actualStartDestination =
            if (destination == Screen.Login) AuthBaseRoute else TasksBaseRoute

        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
            ) {
                NavHost(
                    modifier = modifier,
                    navController = navController,
                    startDestination = actualStartDestination,
                ) {
                    // Auth Section - includes LoginRoute and RegisterRoute
                    authSection(
                        onNavigateToRegister = {
                            navController.navigate(RegisterRoute)
                        },
                        onNavigateToLogin = {
                            navController.popBackStack()
                        },
                        onNavigateToHome = {
                            navController.navigate(TasksBaseRoute) {
                                popUpTo(LoginRoute) {
                                    inclusive = true
                                }
                            }
                        }
                    )

                    // Tasks Section - includes TasksRoute, TaskDetailRoute, and EditTaskRoute
                    tasksSection(
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onTaskClick = { taskId ->
                            navController.navigate(TaskDetailRoute(taskId.toString()))
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onEditTask = { taskId ->
                            navController.navigate(EditTaskRoute(taskId.toString()))
                        },
                        userInitials = appState.userInitials,
                        onProfileClick = { appState.navigateToProfile() }
                    )

                    // Projects Section - includes ProjectsRoute, ProjectDetailRoute, and TaskDetailRoute
                    projectsSection(
                        onProjectClick = { projectId ->
                            navController.navigate(ProjectDetailRoute(projectId))
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onTaskClick = { taskId ->
                            navController.navigate(TaskDetailRoute(taskId.toString()))
                        },
                        taskDetailsDestination = {
                            taskDetailsScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onEditTask = { taskId ->
                                    navController.navigate(EditTaskRoute(taskId.toString()))
                                }
                            )
                        },
                        userInitials = appState.userInitials,
                        onProfileClick = { appState.navigateToProfile() }
                    )

                    // Calendar Section
                    composableWithCompositionLocal<Screen.Calendar>(
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        val sharedTransitionScope = LocalSharedTransitionScope.current
                            ?: throw IllegalStateException("No sharedTransitionScope found")
                        with(sharedTransitionScope) {
                            CalendarScreen(
                                onTaskClick = { taskId ->
                                    navController.navigate(TaskDetailRoute(taskId))
                                },
                                userInitials = appState.userInitials,
                                onProfileClick = { appState.navigateToProfile() }
                            )
                        }
                    }

                    composable<Screen.Profile>(
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        ProfileScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun enterTransaction(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    when {
        initialState.destination.hasRoute(LoginRoute::class) &&
                targetState.destination.hasRoute(RegisterRoute::class) ->
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))

        initialState.destination.hasRoute(RegisterRoute::class) &&
                targetState.destination.hasRoute(LoginRoute::class) ->
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))

        else -> slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(
                durationMillis = 200,
                easing = LinearEasing
            )
        )
    }
}

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

