package com.danioliveira.taskmanager.ui.tasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.empty_task_list
import kmmtaskmanager.composeapp.generated.resources.ic_empty_tasks
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TasksScreen(
    viewModel: TasksViewModel = koinViewModel(),
    navigateToTaskDetail: (Uuid) -> Unit,
    navigateToCreateTask: () -> Unit
) {
    Surface(color = Color(0XFFF1F5F9)) {
        // Create a wrapper for the onAction function that handles navigation
        val onAction: (TasksAction) -> Unit = { action ->
            when (action) {
                is TasksAction.OpenTaskDetails -> {
                    // Handle navigation directly
                    navigateToTaskDetail(action.taskId)
                }

                is TasksAction.OpenCreateTask -> {
                    // Handle navigation to create task
                    navigateToCreateTask()
                }

                else -> {
                    // Pass other actions to the ViewModel
                    viewModel.handleActions(action)
                }
            }
        }

        TasksScreen(
            state = viewModel.state,
            searchText = viewModel.searchQuery,
            onAction = onAction,
            onSearchTextChange = viewModel::updateSearchQuery
        )
    }
}

@Composable
private fun TasksScreen(
    state: TasksState,
    searchText: String,
    onAction: (TasksAction) -> Unit,
    onSearchTextChange: (String) -> Unit
) {

    Scaffold(
        backgroundColor = Color(0xFFF1F5F9),
        topBar = {
            TasksTopBar(
                completedTasks = state.completedTasks,
                totalTasks = state.totalTasks,
                searchText = searchText,
                onSearchTextChange = {
                    onSearchTextChange(it)
                })
        },
        floatingActionButton = { AddTaskButton(onAction) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    // Show full-screen loading indicator when loading
                    LoadingIndicator()
                }

                state.totalTasks == 0 -> EmptyTasksList()
                else -> {
                    // Show a message that tasks are filtered by search
                    if (searchText.isNotBlank()) {
                        Text(
                            text = "Showing progress for tasks matching: \"$searchText\"",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TasksTopBar(
    completedTasks: Int,
    totalTasks: Int,
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "My Tasks",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        YourProgressSection(completedTasks = completedTasks, totalTasks = totalTasks)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            placeholder = { Text("Search tasks...") },
            colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = MaterialTheme.colors.surface)
        )
    }
}

@Composable
fun YourProgressSection(completedTasks: Int, totalTasks: Int) {
    val progress = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colors.primary,
                strokeCap = StrokeCap.Round
            )
            Text(
                text = "$completedTasks of $totalTasks tasks completed",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@Composable
fun EmptyTasksList() {
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
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material.CircularProgressIndicator()
    }
}

@Composable
fun AddTaskButton(
    onAction: (TasksAction) -> Unit
) {
    FloatingActionButton(
        onClick = { onAction(TasksAction.OpenCreateTask) },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = MaterialTheme.colors.onPrimary
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun TasksScreenPreview() {
    TaskItTheme {
        TasksScreen(
            state = TasksState(
                completedTasks = 3,
                totalTasks = 5,
                isLoading = false,
            ),
            searchText = "",
            onAction = {},
            onSearchTextChange = {}
        )
    }
}

@Preview
@Composable
fun EmptyTasksScreenPreview() {
    TaskItTheme {
        TasksScreen(
            state = TasksState(
                completedTasks = 0,
                totalTasks = 0,
                isLoading = false,
            ),
            searchText = "",
            onAction = {},
            onSearchTextChange = {}
        )
    }
}
