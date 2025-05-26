package com.danioliveira.taskmanager.ui.tasks

import kotlin.uuid.Uuid

data class TasksState(
    val isLoading: Boolean = false,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val errorMessage: String? = null
)

sealed interface TasksAction {
    data object LoadTasks : TasksAction
    data object RefreshTasks : TasksAction

    data object OpenCreateTask : TasksAction
    data class OpenTaskDetails(val taskId: Uuid) : TasksAction
    data class UpdateSearchQuery(val query: String) : TasksAction
}
