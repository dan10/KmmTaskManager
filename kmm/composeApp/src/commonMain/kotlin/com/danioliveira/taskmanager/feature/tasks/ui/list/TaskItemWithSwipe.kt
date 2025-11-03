package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.TaskItem
import com.danioliveira.taskmanager.core.ui.theme.TaskItThemeExt


/**
 * Task item with swipe-to-dismiss functionality.
 * Supports swipe right for complete/uncomplete and swipe left for delete.
 *
 * @param task The task to display
 * @param onClick Callback invoked when the task is clicked
 * @param onAction Callback for task actions (for backward compatibility with TasksScreen)
 * @param modifier Modifier to be applied to the component
 * @param showProjectName Whether to show the project name
 * @param onCheckedChange Optional callback for checkbox state changes
 * @param onSwipeComplete Optional callback for swipe-to-complete action
 * @param onSwipeDelete Optional callback for swipe-to-delete action
 */
context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun TaskItemWithSwipe(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showProjectName: Boolean = true,
    onAction: ((TasksAction) -> Unit)? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onSwipeComplete: (() -> Unit)? = null,
    onSwipeDelete: (() -> Unit)? = null
) {
    val swipeToDismissState = rememberSwipeToDismissBoxState()

    // Reset swipe state when task changes (e.g., status updated)
    LaunchedEffect(task.id, task.status) {
        swipeToDismissState.reset()
    }

    val extendedColors = TaskItThemeExt.colors
    
    SwipeToDismissBox(
        modifier = modifier,
        state = swipeToDismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (swipeToDismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> extendedColors.swipeCompleteBackground
                    SwipeToDismissBoxValue.EndToStart -> extendedColors.swipeDeleteBackground
                }
            )

            BoxSwipe(color, swipeToDismissState, extendedColors)
        },
        onDismiss = { direction: SwipeToDismissBoxValue ->
            when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    handleSwipeComplete(task, onSwipeComplete, onAction)
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    handleSwipeDelete(task, onSwipeDelete, onAction)
                }
                else -> {}
            }
        }
    ) {
        TaskItem(
            task = task,
            onClick = onClick,
            onCheckedChange = onCheckedChange ?: {},
            showProjectName = showProjectName
        )
    }
}

private fun handleSwipeComplete(
    task: Task,
    onSwipeComplete: (() -> Unit)?,
    onAction: ((TasksAction) -> Unit)?
) {
    if (onSwipeComplete != null) {
        onSwipeComplete()
    } else if (onAction != null) {
        val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
        onAction(TasksAction.ConfirmTaskCompletion(task.id, newStatus))
    }
}

private fun handleSwipeDelete(
    task: Task,
    onSwipeDelete: (() -> Unit)?,
    onAction: ((TasksAction) -> Unit)?
) {
    if (onSwipeDelete != null) {
        onSwipeDelete()
    } else if (onAction != null) {
        onAction(TasksAction.ConfirmTaskDeletion(task.id))
    }
}

@Composable
private fun BoxSwipe(
    color: Color,
    swipeToDismissState: SwipeToDismissBoxState,
    extendedColors: com.danioliveira.taskmanager.core.ui.theme.TaskItExtendedColors
) {
    Box(Modifier.fillMaxSize().background(color)) {
        when (swipeToDismissState.targetValue) {
            SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Done
            SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
            else -> null
        }?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier
                    .align(
                        when (swipeToDismissState.targetValue) {
                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                            else -> Alignment.CenterStart
                        }
                    )
                    .background(Color.Transparent)
                    .padding(16.dp),
                tint = when (swipeToDismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color.White
                    SwipeToDismissBoxValue.EndToStart -> extendedColors.swipeDeleteForeground
                    else -> Color.White
                }
            )
        }
    }
}