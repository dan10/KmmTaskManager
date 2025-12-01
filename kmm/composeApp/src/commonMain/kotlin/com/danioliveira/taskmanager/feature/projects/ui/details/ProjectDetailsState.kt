package com.danioliveira.taskmanager.feature.projects.ui.details

import com.danioliveira.taskmanager.core.domain.model.Project

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
