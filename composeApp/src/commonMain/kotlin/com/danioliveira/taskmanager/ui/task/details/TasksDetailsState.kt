package com.danioliveira.taskmanager.ui.task.details

import com.danioliveira.taskmanager.domain.Task

data class TasksDetailsState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val errorMessage: String? = null
)

sealed interface TasksDetailsAction {
    data object LoadTaskDetails : TasksDetailsAction
    data class NavigateToFiles(val taskId: String) : TasksDetailsAction
    data object NavigateBack : TasksDetailsAction
}
