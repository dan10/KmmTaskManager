package com.danioliveira.taskmanager.ui.tasks

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.domain.TaskStatus
import kotlin.uuid.Uuid

/**
 * State for the Tasks screen.
 */
data class TasksState(
    val isLoading: Boolean = false,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val searchFieldState: TextFieldState = TextFieldState()
)

/**
 * Actions that can be performed on the Tasks screen.
 */
sealed interface TasksAction {
    data object LoadTasks : TasksAction
    data object RefreshTasks : TasksAction

    data object OpenCreateTask : TasksAction
    data class OpenTaskDetails(val taskId: Uuid) : TasksAction
    data class UpdateTaskStatus(val taskId: Uuid, val status: TaskStatus) : TasksAction
}
