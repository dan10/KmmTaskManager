package com.danioliveira.taskmanager.ui.projects

data class ProjectsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ProjectsAction {
    data object LoadProjects : ProjectsAction
    data object RefreshProjects : ProjectsAction

    data object OpenCreateProject : ProjectsAction
    data class OpenProjectDetails(val projectId: String) : ProjectsAction
}
