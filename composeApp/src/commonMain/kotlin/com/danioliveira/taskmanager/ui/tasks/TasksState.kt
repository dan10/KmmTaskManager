package com.danioliveira.taskmanager.ui.tasks

import com.danioliveira.taskmanager.domain.Task

data class TasksState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null
)

sealed interface TasksAction {
    data object LoadTasks : TasksAction
    data class DeleteTask(val task: Task) : TasksAction
    data object CreateTask : TasksAction
    data class OpenTask(val task: Task) : TasksAction
    data class UpdateTask(val task: Task) : TasksAction
}