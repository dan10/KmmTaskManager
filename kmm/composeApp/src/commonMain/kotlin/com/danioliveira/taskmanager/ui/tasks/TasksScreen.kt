package com.danioliveira.taskmanager.ui.tasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.paging.compose.LazyPagingItems
import com.danioliveira.taskmanager.paging.compose.collectAsLazyPagingItems
import com.danioliveira.taskmanager.paging.compose.itemContentType
import com.danioliveira.taskmanager.paging.compose.itemKey
import com.danioliveira.taskmanager.ui.components.TaskItem
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_search
import kmmtaskmanager.composeapp.generated.resources.empty_task_list
import kmmtaskmanager.composeapp.generated.resources.ic_empty_tasks
import kmmtaskmanager.composeapp.generated.resources.tasks_empty_subtitle
import kmmtaskmanager.composeapp.generated.resources.tasks_empty_title
import kmmtaskmanager.composeapp.generated.resources.tasks_progress_completed
import kmmtaskmanager.composeapp.generated.resources.tasks_progress_percentage
import kmmtaskmanager.composeapp.generated.resources.tasks_progress_title
import kmmtaskmanager.composeapp.generated.resources.tasks_search_placeholder
import kmmtaskmanager.composeapp.generated.resources.tasks_title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
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
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.checkAndRefresh()
    }

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
            pagingItems = viewModel.taskFlow.collectAsLazyPagingItems(),
            searchText = viewModel.searchQuery,
            onAction = onAction,
            onSearchTextChange = viewModel::updateSearchQuery
        )
    }
}

@Composable
private fun TasksScreen(
    state: TasksState,
    pagingItems: LazyPagingItems<Task>,
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
        // Show loading indicator when initial loading
        if (state.isLoading && pagingItems.itemCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.id },
                    contentType = pagingItems.itemContentType { "task" }) { index ->
                    val task = pagingItems[index]
                    if (task != null) {
                        TaskItem(
                            task = task,
                            onClick = { onAction(TasksAction.OpenTaskDetails(task.id)) },
                            onCheckedChange = {},
                        )
                    }
                }

                if (pagingItems.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }

                if (pagingItems.loadState.append.endOfPaginationReached && pagingItems.itemCount == 0) {
                    item {
                        EmptyTasksList()
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
            text = stringResource(Res.string.tasks_title),
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
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = stringResource(Res.string.content_description_search)
                )
            },
            placeholder = { Text(stringResource(Res.string.tasks_search_placeholder)) },
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
                    text = stringResource(Res.string.tasks_progress_title),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(
                        Res.string.tasks_progress_percentage,
                        (progress * 100).toInt()
                    ),
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
                text = stringResource(
                    Res.string.tasks_progress_completed,
                    completedTasks,
                    totalTasks
                ),
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
            text = stringResource(Res.string.tasks_empty_title),
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Message
        Text(
            text = stringResource(Res.string.tasks_empty_subtitle),
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
        CircularProgressIndicator()
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

/**
 * The preview function should be responsible for creating the fake data and passing it to the
 * function that displays it.
 */
@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun TasksScreenPreview() {
    // create list of fake data for preview
    val fakeData = List(10) { index ->
        Task(
            id = Uuid.parse("00000000-0000-0000-0000-00000000000$index"),
            title = "Preview Task $index",
            description = "This is a preview task description",
            projectName = "Preview Project",
            status = if (index < 3) TaskStatus.DONE else TaskStatus.TODO,
            priority = when (index % 3) {
                0 -> Priority.HIGH
                1 -> Priority.MEDIUM
                else -> Priority.LOW
            },
            dueDate = LocalDateTime.parse("2023-12-31T00:00:00")
        )
    }
    // create pagingData from a list of fake data
    val pagingData = PagingData.from(fakeData)
    // pass pagingData containing fake data to a MutableStateFlow
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        TasksScreen(
            state = TasksState(
                completedTasks = 3,
                totalTasks = 10,
                isLoading = false,
            ),
            // pass flow to composable
            pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
            searchText = "",
            onAction = {},
            onSearchTextChange = {}
        )
    }
}

/**
 * The preview function should be responsible for creating the fake data and passing it to the
 * function that displays it.
 */
@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
fun EmptyTasksScreenPreview() {
    // create list of fake data for preview
    val fakeData = emptyList<Task>()
    // create pagingData from a list of fake data
    val pagingData = PagingData.from(fakeData)
    // pass pagingData containing fake data to a MutableStateFlow
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        TasksScreen(
            state = TasksState(
                completedTasks = 0,
                totalTasks = 0,
                isLoading = false,
            ),
            // pass flow to composable
            pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
            searchText = "",
            onAction = {},
            onSearchTextChange = {}
        )
    }
}
