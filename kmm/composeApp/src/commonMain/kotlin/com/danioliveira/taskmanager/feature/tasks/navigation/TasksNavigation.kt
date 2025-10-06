package com.danioliveira.taskmanager.feature.tasks.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.feature.tasks.ui.details.TaskDetailsScreen
import com.danioliveira.taskmanager.feature.tasks.ui.edit.EditTaskScreen
import com.danioliveira.taskmanager.feature.tasks.ui.list.TasksScreen
import com.danioliveira.taskmanager.navigation.Screen
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable data object TasksRoute

@Serializable data class TaskDetailRoute(val taskId: Uuid)

@Serializable data class EditTaskRoute(val taskId: Uuid)

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
    onEditTask: (Uuid) -> Unit
) {
    navigation<TasksBaseRoute>(startDestination = TasksRoute) {
        composable<TasksRoute>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            TasksScreen(
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = this@composable,
                navigateToTaskDetail = onTaskClick
            )
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
    composable<Screen.TasksDetails> { backStackEntry ->
        TaskDetailsScreen(
            onBack = onBackClick,
            onEditTask = { taskId ->
                onEditTask(taskId)
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.editTaskScreen(
    sharedTransitionScope: SharedTransitionScope,
    onBackClick: () -> Unit
) {
    composable<Screen.EditTask> { backStackEntry ->
        EditTaskScreen(
            taskId = backStackEntry.toRoute<EditTaskRoute>().taskId.toString(),
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = this@composable,
            onBack = onBackClick
        )
    }
}