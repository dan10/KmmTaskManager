package com.danioliveira.taskmanager.feature.tasks.ui.details

import com.danioliveira.taskmanager.core.domain.model.Task

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
