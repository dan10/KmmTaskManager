package com.danioliveira.taskmanager.ui.project.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.danioliveira.taskmanager.ui.components.TaskItEmptyState
import com.danioliveira.taskmanager.ui.components.TaskItErrorState
import com.danioliveira.taskmanager.ui.components.TaskItInfoCard
import com.danioliveira.taskmanager.ui.components.TaskItLoadingState
import com.danioliveira.taskmanager.ui.components.TaskItSmallLoadingIndicator
import com.danioliveira.taskmanager.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.ui.components.TaskItem
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
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
    navigateToTaskDetail: (Uuid) -> Unit,
    viewModel: ProjectDetailsViewModel = koinViewModel()
) {
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.checkAndRefresh()
    }

    LaunchedEffect(viewModel) {
        viewModel.onBack = onBack
        viewModel.onCreateTask = {
            navigateToCreateTask(it)
        }
    }

    val state = viewModel.state
    val pagingItems = viewModel.taskFlow.collectAsLazyPagingItems()

    ProjectDetailsScreen(
        state = state,
        onBack = onBack,
        pagingItems = pagingItems,
        navigateToTaskDetail = navigateToTaskDetail,
        actions = viewModel::handleActions
    )
}

@Composable
private fun ProjectDetailsScreen(
    state: ProjectDetailsState,
    onBack: () -> Unit,
    pagingItems: LazyPagingItems<Task>,
    navigateToTaskDetail: (Uuid) -> Unit,
    actions: (ProjectDetailsAction) -> Unit
) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TaskItTopAppBar(
                    title = state.project?.name ?: stringResource(Res.string.project_details_title),
                    onNavigateBack = onBack
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
                    state.isLoading -> TaskItLoadingState()
                    state.errorMessage != null -> TaskItErrorState(state.errorMessage)
                    else -> ProjectDetailsContent(
                        project = state.project,
                        pagingItems = pagingItems,
                        navigateToTaskDetail = navigateToTaskDetail,
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

@Composable
private fun CreateTaskFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(Res.string.content_description_create_task),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun ProjectDetailsContent(
    project: Project?,
    pagingItems: LazyPagingItems<Task>,
    navigateToTaskDetail: (Uuid) -> Unit,
    onTaskStatusChange: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        project?.let {
            ProjectHeader(it)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = stringResource(Res.string.project_tasks_title),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProjectTasksList(
            pagingItems = pagingItems,
            navigateToTaskDetail = navigateToTaskDetail,
            onTaskStatusChange = onTaskStatusChange
        )
    }
}

@Composable
private fun ProjectTasksList(
    pagingItems: LazyPagingItems<Task>,
    navigateToTaskDetail: (Uuid) -> Unit,
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
                    onClick = { navigateToTaskDetail(task.id) },
                    onCheckedChange = { isChecked ->
                        val newStatus =
                            if (isChecked) TaskStatus.DONE.name else TaskStatus.TODO.name
                        onTaskStatusChange(task.id.toString(), newStatus)
                    },
                    showProjectName = false
                )
            }
        }

        if (pagingItems.loadState.append == LoadState.Loading) {
            item {
                TaskItSmallLoadingIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        if (pagingItems.loadState.append.endOfPaginationReached && pagingItems.itemCount == 0) {
            item {
                TaskItEmptyState(
                    title = stringResource(Res.string.project_tasks_empty),
                    message = stringResource(Res.string.project_tasks_empty)
                )
            }
        }
    }
}

@Composable
fun ProjectHeader(project: Project) {
    TaskItInfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ProjectStatus(
                label = stringResource(Res.string.project_status_total_tasks),
                value = project.total
            )
            ProjectStatus(
                label = stringResource(Res.string.project_status_in_progress),
                value = project.inProgress
            )
            ProjectStatus(
                label = stringResource(Res.string.project_status_completed),
                value = project.completed
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.project_progress_title),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))

        val progressPercentage = if (project.total > 0) {
            (project.completed * 100) / project.total
        } else {
            0
        }

        LinearProgressIndicator(
            progress = { progressPercentage / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "$progressPercentage%",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ProjectStatus(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}

@Preview
@Composable
private fun ProjectDetailsTopBarPreview() {
    TaskItTheme {
        TaskItTopAppBar(
            title = "Project Details",
            onNavigateBack = {}
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

    val pagingData = PagingData.from(mockTasks)
    val mockTaskFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        ProjectDetailsScreen(
            state = mockState,
            onBack = {},
            pagingItems = mockTaskFlow.collectAsLazyPagingItems(),
            navigateToTaskDetail = { _ -> },
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

    val pagingData = PagingData.from(mockTasks)
    val mockTaskFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        ProjectDetailsContent(
            project = mockProject,
            pagingItems = mockTaskFlow.collectAsLazyPagingItems(),
            navigateToTaskDetail = { _ -> },
            onTaskStatusChange = { _, _ -> }
        )
    }
}
