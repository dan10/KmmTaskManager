package com.danioliveira.taskmanager.ui.project.details

import com.danioliveira.taskmanager.domain.Project

data class ProjectDetailsState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val errorMessage: String? = null
)

sealed interface ProjectDetailsAction {
    data object RefreshTasks : ProjectDetailsAction
    data class UpdateTaskStatus(val taskId: String, val status: String) : ProjectDetailsAction
    data object CreateTask : ProjectDetailsAction
}
