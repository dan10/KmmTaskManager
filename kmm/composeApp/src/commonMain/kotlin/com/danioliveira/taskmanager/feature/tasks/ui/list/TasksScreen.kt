package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.paging.LoadState
import androidx.paging.PagingData
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.TaskItem
import com.danioliveira.taskmanager.core.ui.components.TaskItemSkeleton
import com.danioliveira.taskmanager.feature.tasks.ui.create.TaskCreateEditBottomSheet
import com.danioliveira.taskmanager.paging.compose.LazyPagingItems
import com.danioliveira.taskmanager.paging.compose.collectAsLazyPagingItems
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import com.danioliveira.taskmanager.util.HapticFeedbackType
import com.danioliveira.taskmanager.util.rememberHapticFeedback
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun TasksScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: TasksViewModel = koinViewModel(),
    navigateToTaskDetail: (Uuid) -> Unit,
) {
    var showCreateTaskBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Refresh on create
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        viewModel.checkAndRefresh()
    }
    
    // Also refresh when returning from other screens (e.g., after deleting a task)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.checkAndRefresh()
    }
        // Create a wrapper for the onAction function that handles navigation
        val onAction: (TasksAction) -> Unit = { action ->
            when (action) {
                is TasksAction.OpenTaskDetails -> {
                    // Handle navigation directly
                    navigateToTaskDetail(action.taskId)
                }

                is TasksAction.OpenCreateTask -> {
                    // Show the BottomSheet instead of navigating
                    showCreateTaskBottomSheet = true
                }

                else -> {
                    // Pass other actions to the ViewModel
                    viewModel.handleActions(action)
                }
            }
        }

        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(viewModel) {
            viewModel.events.collect { event ->
                when (event) {
                    is TasksViewModel.TaskUiEvent.ShowSnackbar -> {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            event.onAction?.invoke()
                        }
                    }
                }
            }
        }

        TasksScreen(
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            state = viewModel.state,
            snackbarHostState = snackbarHostState,
            pagingItems = viewModel.taskFlow.collectAsLazyPagingItems(),
            onAction = onAction,
        )
        
        // Task Create BottomSheet
        if (showCreateTaskBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showCreateTaskBottomSheet = false
                },
                sheetState = sheetState
            ) {
                TaskCreateEditBottomSheet(
                    taskId = null,
                    projectId = null,
                    onDismiss = {
                        showCreateTaskBottomSheet = false
                    }
                )
            }
        }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TasksScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    state: TasksState,
    snackbarHostState: SnackbarHostState,
    pagingItems: LazyPagingItems<Task>,
    onAction: (TasksAction) -> Unit,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val pendingChanges = remember { mutableMapOf<Uuid, TaskStatus>() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TasksTopBar(
                completedTasks = state.completedTasks,
                totalTasks = state.totalTasks,
                searchFieldState = state.searchFieldState,
                onClearSearch = { onAction(TasksAction.ClearSearch) }
            )
        },
        floatingActionButton = { AddTaskButton(onAction) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Filter and Sort Section
            FilterAndSortSection(
                state = state,
                onAction = onAction
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tasks List with Pull to Refresh
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        pagingItems.refresh()
                        delay(500) // Small delay to show the refresh indicator
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                // Apply filtering and sorting
                val filteredItems = remember(pagingItems.itemSnapshotList, state.selectedStatusFilters, state.selectedPriorityFilters, state.sortOption) {
                    pagingItems.itemSnapshotList.items
                        .let { tasks ->
                            // Apply status filter
                            if (state.selectedStatusFilters.isNotEmpty()) {
                                tasks.filter { it.status in state.selectedStatusFilters }
                            } else {
                                tasks
                            }
                        }
                        .let { tasks ->
                            // Apply priority filter
                            if (state.selectedPriorityFilters.isNotEmpty()) {
                                tasks.filter { it.priority in state.selectedPriorityFilters }
                            } else {
                                tasks
                            }
                        }
                        .let { tasks ->
                            // Apply sorting
                            when (state.sortOption) {
                                TaskSortOption.DATE_DESC -> tasks.sortedByDescending { it.dueDate ?: kotlinx.datetime.LocalDateTime(1970, 1, 1, 0, 0) }
                                TaskSortOption.DATE_ASC -> tasks.sortedBy { it.dueDate ?: kotlinx.datetime.LocalDateTime(9999, 12, 31, 23, 59) }
                                TaskSortOption.PRIORITY_HIGH -> tasks.sortedByDescending { it.priority.ordinal }
                                TaskSortOption.PRIORITY_LOW -> tasks.sortedBy { it.priority.ordinal }
                                TaskSortOption.TITLE_AZ -> tasks.sortedBy { it.title.lowercase() }
                                TaskSortOption.TITLE_ZA -> tasks.sortedByDescending { it.title.lowercase() }
                            }
                        }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                items(
                    count = filteredItems.size,
                    key = { index -> filteredItems[index].id }
                ) { index ->
                    val task = filteredItems[index]
                    SwipeActionTaskItem(
                        task = task,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        onToggleStatusRequest = { toggledTask, newStatus ->
                            coroutineScope.launch {
                                pendingChanges[toggledTask.id] = toggledTask.status
                                onAction(TasksAction.UpdateTaskStatus(toggledTask.id, newStatus))
                                val message = if (newStatus == TaskStatus.DONE) "Marked as complete" else "Marked as todo"
                                val undoResult = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (undoResult == SnackbarResult.ActionPerformed) {
                                    pendingChanges[toggledTask.id]?.let { previousStatus ->
                                        onAction(TasksAction.UpdateTaskStatus(toggledTask.id, previousStatus))
                                    }
                                }
                                pendingChanges.remove(toggledTask.id)
                            }
                        },
                        onDeleteRequest = { deletedTask ->
                            coroutineScope.launch {
                                pendingChanges[deletedTask.id] = deletedTask.status
                                val result = snackbarHostState.showSnackbar(
                                    message = "${deletedTask.title} deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result != SnackbarResult.ActionPerformed) {
                                    onAction(TasksAction.DeleteTask(deletedTask.id))
                                }
                                pendingChanges.remove(deletedTask.id)
                            }
                        }
                    )
                }

                // Show loading indicator when loading more items
                if (pagingItems.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    }
                }
                
                // Show skeleton loaders while initial load
                if (pagingItems.loadState.refresh == LoadState.Loading && filteredItems.isEmpty()) {
                    items(6) {
                        TaskItemSkeleton()
                    }
                }

                // Show empty state when no items match filters or no tasks exist
                if (pagingItems.loadState.refresh !is LoadState.Loading && filteredItems.isEmpty()) {
                    item {
                        if (state.selectedStatusFilters.isNotEmpty() || state.selectedPriorityFilters.isNotEmpty()) {
                            // No results for current filters
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "No tasks match your filters",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Try adjusting your filter criteria",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // No tasks at all
                            EmptyTasksList(
                                onGetStarted = { onAction(TasksAction.OpenCreateTask) }
                            )
                        }
                    }
                }
            } // closes LazyColumn
        } // closes PullToRefreshBox
    } // closes Column
} // closes Scaffold
}

@Composable
private fun TasksTopBar(
    completedTasks: Int,
    totalTasks: Int,
    searchFieldState: TextFieldState,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.tasks_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        YourProgressSection(completedTasks = completedTasks, totalTasks = totalTasks)
        Spacer(modifier = Modifier.height(16.dp))
        TasksSearchField(
            searchFieldState = searchFieldState,
            onClearSearch = onClearSearch
        )
    }
}

@Composable
private fun FilterAndSortSection(
    state: TasksState,
    onAction: (TasksAction) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val filterCount = state.selectedStatusFilters.size + state.selectedPriorityFilters.size
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Filter/Sort controls row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter button with badge
            androidx.compose.material3.FilterChip(
                selected = state.isFilterExpanded || filterCount > 0,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.Click)
                    onAction(TasksAction.ToggleFilterExpanded)
                },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Filter",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Filter")
                        if (filterCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = filterCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            )
            
            // Sort dropdown
            var sortExpanded by remember { mutableStateOf(false) }
            Box {
                androidx.compose.material3.FilterChip(
                    selected = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Click)
                        sortExpanded = true
                    },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Sort",
                                modifier = Modifier.size(18.dp)
                            )
                            Text(getSortLabel(state.sortOption))
                        }
                    }
                )

                androidx.compose.material3.DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    TaskSortOption.entries.forEach { option ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(getSortLabel(option)) },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Click)
                                onAction(TasksAction.ChangeSortOption(option))
                                sortExpanded = false
                            },
                            leadingIcon = if (state.sortOption == option) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
        }
        
        // Expandable filter chips
        androidx.compose.animation.AnimatedVisibility(
            visible = state.isFilterExpanded,
            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Status filters
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskStatus.entries.forEach { status ->
                        androidx.compose.material3.FilterChip(
                            selected = state.selectedStatusFilters.contains(status),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Click)
                                onAction(TasksAction.ToggleStatusFilter(status))
                            },
                            label = { Text(status.name.replace("_", " ")) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Priority filters
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.danioliveira.taskmanager.core.domain.model.Priority.entries.forEach { priority ->
                        androidx.compose.material3.FilterChip(
                            selected = state.selectedPriorityFilters.contains(priority),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.Click)
                                onAction(TasksAction.TogglePriorityFilter(priority))
                            },
                            label = { Text(priority.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = when (priority) {
                                                com.danioliveira.taskmanager.core.domain.model.Priority.HIGH -> Color(0xFFEF4444)
                                                com.danioliveira.taskmanager.core.domain.model.Priority.MEDIUM -> Color(0xFFF59E0B)
                                                com.danioliveira.taskmanager.core.domain.model.Priority.LOW -> Color(0xFF10B981)
                                            },
                                            shape = CircleShape
                                        )
                                )
                            }
                        )
                    }
                }
                
                // Clear filters button
                if (filterCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.TextButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.Click)
                            onAction(TasksAction.ClearAllFilters)
                        }
                    ) {
                        Text("Clear all filters")
                    }
                }
            }
        }
    }
}

private fun getSortLabel(sortOption: TaskSortOption): String {
    return when (sortOption) {
        TaskSortOption.DATE_DESC -> "Newest"
        TaskSortOption.DATE_ASC -> "Oldest"
        TaskSortOption.PRIORITY_HIGH -> "High Priority"
        TaskSortOption.PRIORITY_LOW -> "Low Priority"
        TaskSortOption.TITLE_AZ -> "A-Z"
        TaskSortOption.TITLE_ZA -> "Z-A"
    }
}

@Composable
private fun TasksSearchField(
    searchFieldState: TextFieldState,
    onClearSearch: () -> Unit
) {
    val hasText = searchFieldState.text.isNotEmpty()
    
    OutlinedTextField(
        state = searchFieldState,
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Icon(
                Icons.Filled.Search,
                contentDescription = stringResource(Res.string.content_description_search)
            )
        },
        trailingIcon = {
            if (hasText) {
                androidx.compose.material3.IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        placeholder = { Text(stringResource(Res.string.tasks_search_placeholder)) },
        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
fun YourProgressSection(completedTasks: Int, totalTasks: Int) {
    val targetProgress = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 600)
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(Res.string.tasks_progress_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = if (totalTasks == 0) "No tasks yet" else stringResource(Res.string.tasks_progress_completed, completedTasks, totalTasks),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "$completedTasks/$totalTasks",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                        .background(gradient)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (totalTasks == 0) "Welcome! Let's add your first task." else "You're making steady progress",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.tasks_progress_percentage, (targetProgress * 100).roundToInt()),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun YourProgressSectionPreview() {
    YourProgressSection(completedTasks = 10, totalTasks = 1)
}

@Composable
fun EmptyTasksList(
    onGetStarted: () -> Unit = {}
) {
    val haptic = rememberHapticFeedback()
    
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
        
        // Get Started Button
        Button(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.Click)
                onGetStarted()
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Your First Task")
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
    val haptic = rememberHapticFeedback()
    
    FloatingActionButton(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.Click)
            onAction(TasksAction.OpenCreateTask) 
        },
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Task",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SwipeActionTaskItem(
    task: Task,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onToggleStatusRequest: (Task, TaskStatus) -> Unit,
    onDeleteRequest: (Task) -> Unit
) {
    val haptic = rememberHapticFeedback()
    val dismissState = rememberDismissState()
    val coroutineScope = rememberCoroutineScope()

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> SwipeBackground(
                    icon = Icons.Default.Done,
                    text = if (task.status == TaskStatus.DONE) "Reopen" else "Complete",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    alignment = Alignment.CenterStart
                )
                DismissDirection.EndToStart -> SwipeBackground(
                    icon = Icons.Default.Delete,
                    text = "Delete",
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828),
                    alignment = Alignment.CenterEnd
                )
                else -> Box(modifier = Modifier.fillMaxSize())
            }
        },
        dismissContent = {
            when {
                dismissState.isDismissed(DismissDirection.EndToStart) -> {
                    haptic.performHapticFeedback(HapticFeedbackType.Error)
                    coroutineScope.launch { dismissState.reset() }
                    onDeleteRequest(task)
                }
                dismissState.isDismissed(DismissDirection.StartToEnd) -> {
                    val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
                    haptic.performHapticFeedback(HapticFeedbackType.Success)
                    coroutineScope.launch { dismissState.reset() }
                    onToggleStatusRequest(task, newStatus)
                }
            }

            with(sharedTransitionScope) {
                TaskItem(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "task-card-${task.id}"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                        ),
                    task = task,
                    onClick = { onDeleteRequest(task) },
                    onCheckedChange = { },
                )
            }
        }
    )
}

@Composable
private fun SwipeBackground(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    alignment: Alignment
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(containerColor)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Text(text = text, color = contentColor, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * The preview function should be responsible for creating the fake data and passing it to the
 * function that displays it.
 */
//@OptIn(ExperimentalUuidApi::class)
//@Preview
//@Composable
//fun TasksScreenPreview() {
//    // create list of fake data for preview
//    val fakeData = List(10) { index ->
//        Task(
//            id = Uuid.parse("00000000-0000-0000-0000-00000000000$index"),
//            title = "Preview Task $index",
//            description = "This is a preview task description",
//            projectName = "Preview Project",
//            status = if (index < 3) TaskStatus.DONE else TaskStatus.TODO,
//            priority = when (index % 3) {
//                0 -> Priority.HIGH
//                1 -> Priority.MEDIUM
//                else -> Priority.LOW
//            },
//            dueDate = LocalDateTime.parse("2023-12-31T00:00:00")
//        )
//    }
//    // create pagingData from a list of fake data
//    val pagingData = PagingData.from(fakeData)
//    // pass pagingData containing fake data to a MutableStateFlow
//    val fakeDataFlow = MutableStateFlow(pagingData)
//
//    TaskItTheme {
//        SharedTransitionLayout {
//            TasksScreen(
//                sharedTransitionScope = this,
//                animatedContentScope = this,
//                state = TasksState(
//                    completedTasks = 3,
//                    totalTasks = 10,
//                    isLoading = false,
//                ),
//                // pass flow to composable
//                pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
//                onAction = {},
//                onEditTask = {}
//            )
//        }
//    }
//}

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
                TasksScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    state = TasksState(
                        completedTasks = 0,
                        totalTasks = 0,
                        isLoading = false,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    // pass flow to composable
                    pagingItems = fakeDataFlow.collectAsLazyPagingItems(),
                    onAction = {},
                )
            }
        }
    }
}
