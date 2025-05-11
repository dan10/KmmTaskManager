package com.danioliveira.taskmanager.ui.projects

import androidx.compose.foundation.text.input.TextFieldState

data class ProjectsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchFieldState: TextFieldState = TextFieldState()
)

sealed interface ProjectsAction {
    data object LoadProjects : ProjectsAction
    data object RefreshProjects : ProjectsAction

    data object OpenCreateProject : ProjectsAction
    data class OpenProjectDetails(val projectId: String) : ProjectsAction
    data class UpdateSearchQuery(val query: String) : ProjectsAction
}
