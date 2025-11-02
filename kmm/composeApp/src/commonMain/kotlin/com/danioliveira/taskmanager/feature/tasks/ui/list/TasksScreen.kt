package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.PrincipalTaskItTopAppBar
import com.danioliveira.taskmanager.core.ui.components.TaskItemSkeleton
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskCreateBottomSheet
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_add_task
import kmmtaskmanager.composeapp.generated.resources.empty_task_list
import kmmtaskmanager.composeapp.generated.resources.ic_empty_tasks
import kmmtaskmanager.composeapp.generated.resources.tasks_empty_subtitle
import kmmtaskmanager.composeapp.generated.resources.tasks_empty_title
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

context(_: SharedTransitionScope, _: AnimatedVisibilityScope)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = koinViewModel(),
    navigateToTaskDetail: (Uuid) -> Unit,
    globalSearchQuery: String = "",
    onGlobalSearch: (String) -> Unit = {}
) {
    var showCreateTaskBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Sync global search query with local search field
    LaunchedEffect(globalSearchQuery) {
        viewModel.updateSearchQuery(globalSearchQuery)
    }

    // Refresh when returning from other screens
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkAndRefresh()
    }

    TasksEffectHandler(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onNavigateToTaskDetail = navigateToTaskDetail,
        onShowCreateTaskBottomSheet = { showCreateTaskBottomSheet = true }
    )

    TasksContent(
        pagingItems = viewModel.taskFlow.collectAsLazyPagingItems(),
        state = viewModel.state,
        onAction = viewModel::handleActions,
        snackbarHostState = snackbarHostState,
        onGlobalSearch = onGlobalSearch
    )

    if (showCreateTaskBottomSheet) {
        TaskCreateBottomSheet(
            onDismiss = { shouldRefresh ->
                showCreateTaskBottomSheet = false
                if (shouldRefresh) {
                    viewModel.refresh()
                }
            }
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun TasksTopBar(onGlobalSearch: (String) -> Unit) {
    with(sts) {
        PrincipalTaskItTopAppBar(
            title = stringResource(Res.string.tasks_title),
            onSearch = onGlobalSearch,
            modifier = Modifier.sharedBounds(
                sts.rememberSharedContentState(key = "main_top_bar"),
                avs
            )
        )
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun TasksContent(
    pagingItems: LazyPagingItems<Task>,
    state: TasksState,
    onAction: (TasksAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onGlobalSearch: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TasksTopBar(onGlobalSearch = onGlobalSearch)
        },
        floatingActionButton = { AddTaskButton(onAction) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier.padding(innerPadding),
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(TasksAction.RefreshTasks) },
        ) {
            TaskList(
                pagingItems = pagingItems,
                state = state,
                onAction = onAction
            )
        }
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun TaskList(
    pagingItems: LazyPagingItems<Task>,
    state: TasksState,
    onAction: (TasksAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ProgressSummaryItem(
                completedTasks = state.completedTasks,
                totalTasks = state.totalTasks,
            )
        }

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.id }
        ) { index ->
            val task = pagingItems[index]
            if (task != null) {
                TaskItemWithSwipe(
                    modifier = Modifier.animateItem(),
                    task = task,
                    onClick = { onAction(TasksAction.OpenTaskDetails(it.id)) },
                    onAction = onAction
                )
            } else {
                TaskItemSkeleton()
            }
        }

        if (pagingItems.loadState.isIdle && pagingItems.itemCount == 0) {
            item {
                EmptyTasksList()
            }
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
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Message
        Text(
            text = stringResource(Res.string.tasks_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun AddTaskButton(
    onAction: (TasksAction) -> Unit
) {
    with(sts) {
        FloatingActionButton(
            onClick = {
                onAction(TasksAction.OpenCreateTask)
            },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.sharedElement(
                sts.rememberSharedContentState(key = "add_fab"),
                avs
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.content_description_add_task),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
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
            dueDate = LocalDateTime.parse("2023-12-31T00:00:00"),
            createdAt = LocalDateTime.parse("2023-01-01T00:00:00")
        )
    }
    // create pagingData from a list of fake data
    val pagingData = PagingData.from(fakeData)
    // pass pagingData containing fake data to a MutableStateFlow
    val fakeDataFlow = MutableStateFlow(pagingData)

    TaskItTheme {
        SharedTransitionLayout {
            AnimatedVisibility(true) {
                TasksContent(
                    state = TasksState(
                        completedTasks = 3,
                        totalTasks = 10,
                        isLoading = false,
                    ),
                    // pass flow to composable
                    pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
                    onAction = {},
                )
            }
        }
    }
}

/**
 * The preview function should be responsible for creating the fake data and passing it to the
 * function that displays it.
 */
@OptIn(ExperimentalUuidApi::class, ExperimentalSharedTransitionApi::class)
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
        SharedTransitionLayout {
            AnimatedContent(targetState = Unit) {
                TasksContent(
                    state = TasksState(
                        completedTasks = 0,
                        totalTasks = 0,
                        isLoading = false,
                    ),
                    // pass flow to composable
                    pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
                    onAction = {},
                )
            }
        }
    }
}
