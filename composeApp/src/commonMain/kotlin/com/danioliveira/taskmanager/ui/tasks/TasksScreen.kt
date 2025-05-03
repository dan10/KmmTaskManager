package com.danioliveira.taskmanager.ui.tasks

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.ui.components.TaskItem
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.empty_task_list
import kmmtaskmanager.composeapp.generated.resources.ic_empty_tasks
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun TasksScreen(viewModel: TasksViewModel) {
    TasksScreen(
        state = viewModel.state,
        onAction = viewModel::handleActions
    )
}

@Composable
fun TasksScreen(
    state: TasksState,
    onAction: (TasksAction) -> Unit
) {
    Scaffold(
        floatingActionButton = { AddTaskButton(onAction) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
           when {
               state.isLoading -> {}
               state.tasks.isEmpty() -> EmptyTasksList()
               else -> TasksList(state.tasks, onAction)
           }
        }

    }
}

@Composable
fun EmptyTasksList() {
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Image(
            painter = painterResource(Res.drawable.ic_empty_tasks),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    alpha = alphaAnim
                }
                .padding(bottom = 24.dp)
        )

        // Title
        Text(
            text = "Ready to Get Started?",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Message
        Text(
            text = "Here are some ideas to help you begin:",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stringArrayResource(Res.array.empty_task_list).forEach { suggestion ->
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TasksList(
    tasks: List<Task>,
    onAction: (TasksAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = tasks,
            key = {  it.id }
        ) { task ->
            TaskItem(
                task = task,
                onClick = { onAction(TasksAction.OpenTask(task)) },
                onCheckedChange = { isChecked ->
                    onAction(TasksAction.UpdateTask(task.copy(status = TaskStatus.DONE)))
                }
            )
        }
    }
}



@Composable
fun AddTaskButton(
    onAction: (TasksAction) -> Unit
) {
    FloatingActionButton(
        onClick = { onAction(TasksAction.CreateTask) },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = MaterialTheme.colors.onPrimary
        )
    }
}



@Preview
@Composable
fun EmptyTasksScreenPreview() {
    TaskItTheme {
        TasksScreen(
            state = TasksState(
                tasks = emptyList(),
                isLoading = false,
            ),
            onAction = {}
        )
    }
}

