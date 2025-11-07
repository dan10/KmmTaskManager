package com.danioliveira.taskmanager.feature.tasks.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.LocalSharedTransitionScope
import com.danioliveira.taskmanager.feature.tasks.ui.details.TaskDetailsScreen
import com.danioliveira.taskmanager.feature.tasks.ui.edit.EditTaskScreen
import com.danioliveira.taskmanager.feature.tasks.ui.list.TasksScreen
import com.danioliveira.taskmanager.navigation.composableWithCompositionLocal
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable data object TasksRoute

@Serializable data class TaskDetailRoute(val taskId: String)

@Serializable data class EditTaskRoute(val taskId: String)

@Serializable data object TasksBaseRoute

fun NavController.navigateToTasks(navOptions: NavOptions) = navigate(TasksRoute, navOptions)

/**
 * Adds the tasks section to the navigation graph.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.tasksSection(
    sharedTransitionScope: SharedTransitionScope,
    onTaskClick: (Uuid) -> Unit,
    onBackClick: () -> Unit,
    onEditTask: (Uuid) -> Unit,
    userInitials: String? = null,
    onProfileClick: (() -> Unit)? = null
) {
    navigation<TasksBaseRoute>(startDestination = TasksRoute) {
        composableWithCompositionLocal<TasksRoute>(
            enterTransition = {
                if (initialState.destination.hasRoute<TaskDetailRoute>()) {
                    fadeIn()
                } else {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                if (targetState.destination.hasRoute<TaskDetailRoute>()) {
                    fadeOut(animationSpec = spring())
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            }
        ) {
            val sharedTransitionScope = LocalSharedTransitionScope.current
                ?: throw IllegalStateException("No sharedTransitionScope found")
            with(sharedTransitionScope) {
                TasksScreen(
                    navigateToTaskDetail = onTaskClick,
                    userInitials = userInitials,
                    onProfileClick = onProfileClick
                )
            }
        }
        taskDetailsScreen(
            onBackClick = onBackClick,
            onEditTask = onEditTask
        )
        editTaskScreen(
            sharedTransitionScope = sharedTransitionScope,
            onBackClick = onBackClick
        )
    }
}

fun NavGraphBuilder.taskDetailsScreen(
    onBackClick: () -> Unit,
    onEditTask: (Uuid) -> Unit
) {
    composableWithCompositionLocal<TaskDetailRoute>(
        enterTransition = {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(400))
        }
    ) { backStackEntry ->
        val sharedTransitionScope = LocalSharedTransitionScope.current
            ?: throw IllegalStateException("No sharedTransitionScope found")
        with(sharedTransitionScope) {
            TaskDetailsScreen(
                onBack = onBackClick,
                onEditTask = onEditTask
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.editTaskScreen(
    sharedTransitionScope: SharedTransitionScope,
    onBackClick: () -> Unit
) {
    composable<EditTaskRoute> { backStackEntry ->
        EditTaskScreen(
            taskId = backStackEntry.toRoute<EditTaskRoute>().taskId.toString(),
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this@composable,
            onBack = onBackClick
        )
    }
}