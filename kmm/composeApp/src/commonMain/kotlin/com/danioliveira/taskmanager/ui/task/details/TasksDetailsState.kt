package com.danioliveira.taskmanager.ui.task.details

import com.danioliveira.taskmanager.domain.Task

data class TasksDetailsState(
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val task: Task? = null,
    val errorMessage: String? = null
)

sealed interface TasksDetailsAction {
    data object LoadTaskDetails : TasksDetailsAction
    data object NavigateBack : TasksDetailsAction
    data object EditTask : TasksDetailsAction
    data object DeleteTask : TasksDetailsAction
}
