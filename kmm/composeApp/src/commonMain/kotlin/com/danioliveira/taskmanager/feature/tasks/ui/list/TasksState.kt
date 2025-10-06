package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import kotlin.uuid.Uuid

/**
 * Sorting options for tasks
 */

/**
 * State for the Tasks screen.
 */
data class TasksState(
    val isLoading: Boolean = false,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val searchFieldState: TextFieldState = TextFieldState(),
    val selectedStatusFilters: Set<TaskStatus> = emptySet(),
    val selectedPriorityFilters: Set<Priority> = emptySet(),
    val isRefreshing: Boolean = false,
    val isFilterExpanded: Boolean = false
) {
}

/**
 * Actions that can be performed on the Tasks screen.
 */
sealed interface TasksAction {
    data object LoadTasks : TasksAction
    data object RefreshTasks : TasksAction
    data object OpenCreateTask : TasksAction
    data class OpenTaskDetails(val taskId: Uuid) : TasksAction
    data class UpdateTaskStatus(val taskId: Uuid, val status: TaskStatus) : TasksAction
    data class DeleteTask(val taskId: Uuid) : TasksAction
}
