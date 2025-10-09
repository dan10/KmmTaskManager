package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.TaskItem


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TaskItemWithSwipe(
    task: Task,
    onClick: (Task) -> Unit,
    onAction: (TasksAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeToDismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        modifier = modifier,
        state = swipeToDismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (swipeToDismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF2E7D32)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFFFEBEE)
                }
            )

            BoxSwipe(color, swipeToDismissState)
        },
        onDismiss = { direction ->
            when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
                    onAction(TasksAction.ConfirmTaskCompletion(task.id, newStatus))
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onAction(TasksAction.ConfirmTaskDeletion(task.id))
                }
                else -> Unit
            }
        }
    ) {
        TaskItem(
            task = task,
            onClick = { onClick(task) },
            onCheckedChange = {}
        )
    }
}

@Composable
private fun BoxSwipe(
    color: Color,
    swipeToDismissState: SwipeToDismissBoxState
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
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFD32F2F)
                    else -> Color.White
                }
            )
        }
    }
}