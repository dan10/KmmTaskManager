package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import kotlin.uuid.Uuid

/**
 * Sorting options for tasks
 */
enum class TaskSortOption {
    DATE_DESC,      // Newest first
    DATE_ASC,       // Oldest first
    PRIORITY_HIGH,  // High priority first
    PRIORITY_LOW,   // Low priority first
    TITLE_AZ,       // A-Z
    TITLE_ZA        // Z-A
}

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
    val sortOption: TaskSortOption = TaskSortOption.DATE_DESC,
    val isRefreshing: Boolean = false,
    val isFilterExpanded: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = selectedStatusFilters.isNotEmpty() || selectedPriorityFilters.isNotEmpty()
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
    data class SetSearchQuery(val query: String) : TasksAction
    
    // Search and filter actions
    data object ClearSearch : TasksAction
    data class ToggleStatusFilter(val status: TaskStatus) : TasksAction
    data class TogglePriorityFilter(val priority: Priority) : TasksAction
    data class ChangeSortOption(val sortOption: TaskSortOption) : TasksAction
    data object ClearAllFilters : TasksAction
    data object ToggleFilterExpanded : TasksAction
    data object Refresh : TasksAction
}
