package com.danioliveira.taskmanager.ui.project.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.Project
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.paging.compose.LazyPagingItems
import com.danioliveira.taskmanager.paging.compose.collectAsLazyPagingItems
import com.danioliveira.taskmanager.paging.compose.itemContentType
import com.danioliveira.taskmanager.paging.compose.itemKey
import com.danioliveira.taskmanager.ui.components.TaskItem
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.content_description_create_task
import kmmtaskmanager.composeapp.generated.resources.project_details_title
import kmmtaskmanager.composeapp.generated.resources.project_progress_title
import kmmtaskmanager.composeapp.generated.resources.project_status_completed
import kmmtaskmanager.composeapp.generated.resources.project_status_in_progress
import kmmtaskmanager.composeapp.generated.resources.project_status_total_tasks
import kmmtaskmanager.composeapp.generated.resources.project_tasks_empty
import kmmtaskmanager.composeapp.generated.resources.project_tasks_title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ProjectDetailsScreen(
    onBack: () -> Unit,
    navigateToCreateTask: (String) -> Unit,
    viewModel: ProjectDetailsViewModel = koinViewModel()
) {
    // Refresh data when screen is created
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.checkAndRefresh()
    }

    // Set up navigation callback
    LaunchedEffect(viewModel) {
        viewModel.onBack = onBack
        viewModel.onCreateTask = {
            navigateToCreateTask(it)
        }
    }

    // Get the current state
    val state = viewModel.state
    val pagingItems = viewModel.taskFlow.collectAsLazyPagingItems()

    ProjectDetailsScreen(
        state = state,
        onBack = onBack,
        pagingItems = pagingItems,
        actions = viewModel::handleActions
    )
}

@Composable
private fun ProjectDetailsScreen(
    state: ProjectDetailsState,
    onBack: () -> Unit,
    pagingItems: LazyPagingItems<Task>,
    actions: (ProjectDetailsAction) -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
                .navigationBarsPadding(),
            topBar = {
                ProjectDetailsTopBar(
                    title = state.project?.name ?: stringResource(Res.string.project_details_title),
                    onBack = onBack
                )
            },
            floatingActionButton = {
                CreateTaskFAB(
                    onClick = { actions(ProjectDetailsAction.CreateTask) }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    state.isLoading -> LoadingState()
                    state.errorMessage != null -> ErrorState(errorMessage = state.errorMessage)
                    else -> ProjectDetailsContent(
                        project = state.project,
                        pagingItems = pagingItems,
                        onTaskStatusChange = { taskId, status ->
                            actions(
                                ProjectDetailsAction.UpdateTaskStatus(
                                    taskId = taskId,
                                    status = status
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectDetailsTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        }
    )
}

@Composable
private fun CreateTaskFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(Res.string.content_description_create_task),
            tint = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = errorMessage)
    }
}

@Composable
private fun ProjectDetailsContent(
    project: Project?,
    pagingItems: LazyPagingItems<Task>,
    onTaskStatusChange: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Project Header
        project?.let {
            ProjectHeader(it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = stringResource(Res.string.project_tasks_title),
            style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Task list
        ProjectTasksList(
            pagingItems = pagingItems,
            onTaskStatusChange = onTaskStatusChange
        )
    }
}

@Composable
private fun ProjectTasksList(
    pagingItems: LazyPagingItems<Task>,
    onTaskStatusChange: (String, String) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id },
            contentType = pagingItems.itemContentType { "task" }
        ) { index ->
            val task = pagingItems[index]
            if (task != null) {
                TaskItem(
                    task = task,
                    onClick = { /* No-op for now */ },
                    onCheckedChange = { isChecked ->
                        val newStatus = if (isChecked) TaskStatus.DONE.name else TaskStatus.TODO.name
                        onTaskStatusChange(task.id.toString(), newStatus)
                    }
                )
            }
        }

        // Loading indicator
        if (pagingItems.loadState.append == LoadState.Loading) {
            item {
                LoadingIndicator()
            }
        }

        // Empty state
        if (pagingItems.loadState.append.endOfPaginationReached && pagingItems.itemCount == 0) {
            item {
                EmptyTasksMessage()
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyTasksMessage() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(Res.string.project_tasks_empty))
    }
}

@Composable
fun ProjectHeader(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProjectStatus(label = stringResource(Res.string.project_status_total_tasks), value = project.total)
                ProjectStatus(label = stringResource(Res.string.project_status_in_progress), value = project.inProgress)
                ProjectStatus(label = stringResource(Res.string.project_status_completed), value = project.completed)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(Res.string.project_progress_title), style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(4.dp))

            // Calculate progress percentage
            val progressPercentage = if (project.total > 0) {
                (project.completed * 100) / project.total
            } else {
                0
            }

            LinearProgressIndicator(
                progress = progressPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$progressPercentage%",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun ProjectStatus(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.caption)
    }
}

@Preview
@Composable
private fun LoadingStatePreview() {
    TaskItTheme {
        LoadingState()
    }
}

@Preview
@Composable
private fun ErrorStatePreview() {
    TaskItTheme {
        ErrorState(errorMessage = "Failed to load project details")
    }
}

@Preview
@Composable
private fun LoadingIndicatorPreview() {
    TaskItTheme {
        LoadingIndicator()
    }
}

@Preview
@Composable
private fun EmptyTasksMessagePreview() {
    TaskItTheme {
        EmptyTasksMessage()
    }
}

@Preview
@Composable
private fun ProjectDetailsTopBarPreview() {
    TaskItTheme {
        ProjectDetailsTopBar(
            title = "Project Details",
            onBack = {}
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun ProjectDetailsScreenPreview() {
    val mockProject = Project(
        id = "project-1",
        name = "Website Redesign",
        completed = 5,
        inProgress = 3,
        total = 10,
        description = "Redesign the company website"
    )

    val mockState = ProjectDetailsState(
        isLoading = false,
        project = mockProject,
        errorMessage = null
    )

    // Create mock tasks
    val mockTasks = List(5) { index ->
        Task(
            id = Uuid.random(),
            title = "Task ${index + 1}",
            description = "Description for task ${index + 1}",
            status = when (index % 3) {
                0 -> TaskStatus.TODO
                1 -> TaskStatus.IN_PROGRESS
                else -> TaskStatus.DONE
            },
            priority = when (index % 3) {
                0 -> Priority.HIGH
                1 -> Priority.MEDIUM
                else -> Priority.LOW
            },
            dueDate = LocalDateTime.parse("2024-12-${index + 10}T00:00:00"),
            projectName = "Website Redesign"
        )
    }

    // Create pagingData from a list of mock tasks
    val pagingData = PagingData.from(mockTasks)
    // Pass pagingData containing mock tasks to a MutableStateFlow
    val mockTaskFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        ProjectDetailsScreen(
            state = mockState,
            onBack = {},
            pagingItems = mockTaskFlow.collectAsLazyPagingItems(),
            actions = {}
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun ProjectDetailsContentPreview() {
    val mockProject = Project(
        id = "project-1",
        name = "Website Redesign",
        completed = 5,
        inProgress = 3,
        total = 10,
        description = "Redesign the company website"
    )

    // Create mock tasks
    val mockTasks = List(5) { index ->
        Task(
            id = Uuid.random(),
            title = "Task ${index + 1}",
            description = "Description for task ${index + 1}",
            status = when (index % 3) {
                0 -> TaskStatus.TODO
                1 -> TaskStatus.IN_PROGRESS
                else -> TaskStatus.DONE
            },
            priority = when (index % 3) {
                0 -> Priority.HIGH
                1 -> Priority.MEDIUM
                else -> Priority.LOW
            },
            dueDate = LocalDateTime.parse("2024-12-${index + 10}T00:00:00"),
            projectName = "Website Redesign"
        )
    }

    // Create pagingData from a list of mock tasks
    val pagingData = PagingData.from(mockTasks)
    // Pass pagingData containing mock tasks to a MutableStateFlow
    val mockTaskFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        ProjectDetailsContent(
            project = mockProject,
            pagingItems = mockTaskFlow.collectAsLazyPagingItems(),
            onTaskStatusChange = { _, _ -> }
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun ProjectTasksListPreview() {
    // Create mock tasks
    val mockTasks = List(5) { index ->
        Task(
            id = Uuid.random(),
            title = "Task ${index + 1}",
            description = "Description for task ${index + 1}",
            status = when (index % 3) {
                0 -> TaskStatus.TODO
                1 -> TaskStatus.IN_PROGRESS
                else -> TaskStatus.DONE
            },
            priority = when (index % 3) {
                0 -> Priority.HIGH
                1 -> Priority.MEDIUM
                else -> Priority.LOW
            },
            dueDate = LocalDateTime.parse("2024-12-${index + 10}T00:00:00"),
            projectName = "Website Redesign"
        )
    }

    // Create pagingData from a list of mock tasks
    val pagingData = PagingData.from(mockTasks)
    // Pass pagingData containing mock tasks to a MutableStateFlow
    val mockTaskFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        Surface {
            ProjectTasksList(
                pagingItems = mockTaskFlow.collectAsLazyPagingItems(),
                onTaskStatusChange = { _, _ -> }
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun EmptyProjectTasksListPreview() {
    // Create pagingData from an empty list
    val pagingData = PagingData.from(emptyList<Task>())
    // Pass pagingData containing empty list to a MutableStateFlow
    val mockTaskFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        Surface {
            ProjectTasksList(
                pagingItems = mockTaskFlow.collectAsLazyPagingItems(),
                onTaskStatusChange = { _, _ -> }
            )
        }
    }
}
