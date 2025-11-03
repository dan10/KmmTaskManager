package com.danioliveira.taskmanager.core.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.danioliveira.taskmanager.core.domain.model.Task
import com.danioliveira.taskmanager.feature.tasks.ui.list.TaskItemWithSwipe

/**
 * Shared task list component that displays tasks in a lazy column.
 * Supports both regular lists and Paging3 data sources.
 *
 * @param modifier Modifier to be applied to the LazyColumn
 * @param contentPadding Padding values for the list content
 * @param verticalArrangement Vertical arrangement strategy for list items
 * @param enableSwipe Whether to enable swipe-to-dismiss functionality
 * @param showProjectName Whether to show project names in task items
 * @param onTaskClick Callback invoked when a task is clicked
 * @param onTaskCheckedChange Callback invoked when task checkbox state changes
 * @param onTaskSwipeComplete Optional callback for swipe-to-complete action (requires enableSwipe = true)
 * @param onTaskSwipeDelete Optional callback for swipe-to-delete action (requires enableSwipe = true)
 * @param header Optional composable to display before the task list
 * @param emptyContent Optional composable to display when the list is empty
 */
context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun TaskList(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    enableSwipe: Boolean = false,
    showProjectName: Boolean = true,
    onTaskClick: (Task) -> Unit = {},
    onTaskCheckedChange: (String, Boolean) -> Unit = { _, _ -> },
    onTaskSwipeComplete: ((Task) -> Unit)? = null,
    onTaskSwipeDelete: ((Task) -> Unit)? = null,
    header: (LazyListScope.() -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        header?.invoke(this)

        if (tasks.isEmpty() && emptyContent != null) {
            item {
                emptyContent()
            }
        } else {
            items(
                items = tasks,
                key = { it.id }
            ) { task ->
                TaskListItem(
                    task = task,
                    enableSwipe = enableSwipe,
                    showProjectName = showProjectName,
                    onTaskClick = onTaskClick,
                    onTaskCheckedChange = onTaskCheckedChange,
                    onTaskSwipeComplete = onTaskSwipeComplete,
                    onTaskSwipeDelete = onTaskSwipeDelete,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/**
 * Shared task list component for Paging3 data sources.
 * Displays tasks with pagination support and loading states.
 *
 * @param pagingItems LazyPagingItems containing the task data
 * @param modifier Modifier to be applied to the LazyColumn
 * @param contentPadding Padding values for the list content
 * @param verticalArrangement Vertical arrangement strategy for list items
 * @param enableSwipe Whether to enable swipe-to-dismiss functionality
 * @param showProjectName Whether to show project names in task items
 * @param onTaskClick Callback invoked when a task is clicked
 * @param onTaskCheckedChange Callback invoked when task checkbox state changes
 * @param onTaskSwipeComplete Optional callback for swipe-to-complete action (requires enableSwipe = true)
 * @param onTaskSwipeDelete Optional callback for swipe-to-delete action (requires enableSwipe = true)
 * @param header Optional composable to display before the task list
 * @param emptyContent Optional composable to display when the list is empty
 * @param loadingContent Optional composable to display as placeholder while loading
 */
context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
fun TaskListPaging(
    pagingItems: LazyPagingItems<Task>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    enableSwipe: Boolean = false,
    showProjectName: Boolean = true,
    onTaskClick: (Task) -> Unit = {},
    onTaskCheckedChange: (String, Boolean) -> Unit = { _, _ -> },
    onTaskSwipeComplete: ((Task) -> Unit)? = null,
    onTaskSwipeDelete: ((Task) -> Unit)? = null,
    header: (LazyListScope.() -> Unit)? = null,
    emptyContent: (@Composable () -> Unit)? = null,
    loadingContent: (@Composable () -> Unit)? = { TaskItemSkeleton() }
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        header?.invoke(this)

        if (pagingItems.loadState.isIdle && pagingItems.itemCount == 0 && emptyContent != null) {
            item {
                emptyContent()
            }
        } else {
            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.id }
            ) { index ->
                val task = pagingItems[index]
                if (task != null) {
                    TaskListItem(
                        task = task,
                        enableSwipe = enableSwipe,
                        showProjectName = showProjectName,
                        onTaskClick = onTaskClick,
                        onTaskCheckedChange = onTaskCheckedChange,
                        onTaskSwipeComplete = onTaskSwipeComplete,
                        onTaskSwipeDelete = onTaskSwipeDelete,
                        modifier = Modifier.animateItem()
                    )
                } else if (loadingContent != null) {
                    loadingContent()
                }
            }
        }
    }
}

/**
 * Internal composable that renders a single task item with optional swipe functionality.
 */
context(sts: SharedTransitionScope, avs: AnimatedVisibilityScope)
@Composable
private fun TaskListItem(
    task: Task,
    enableSwipe: Boolean,
    showProjectName: Boolean,
    onTaskClick: (Task) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    onTaskSwipeComplete: ((Task) -> Unit)?,
    onTaskSwipeDelete: ((Task) -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (enableSwipe && (onTaskSwipeComplete != null || onTaskSwipeDelete != null)) {
        TaskItemWithSwipe(
            task = task,
            showProjectName = showProjectName,
            onClick = { onTaskClick(task) },
            onCheckedChange = { checked: Boolean ->
                onTaskCheckedChange(task.id.toString(), checked)
            },
            onSwipeComplete = onTaskSwipeComplete?.let { { it(task) } },
            onSwipeDelete = onTaskSwipeDelete?.let { { it(task) } },
            modifier = modifier
        )
    } else {
        TaskItem(
            task = task,
            showProjectName = showProjectName,
            onClick = { onTaskClick(task) },
            onCheckedChange = { checked: Boolean ->
                onTaskCheckedChange(task.id.toString(), checked)
            },
            modifier = modifier
        )
    }
}

