package com.danioliveira.taskmanager.ui.projects

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectsUseCase

class ProjectsViewModel(
    private val getProjectsUseCase: GetProjectsUseCase
) : ViewModel() {

    var state by mutableStateOf(ProjectsState())
        private set

    val projectFlow = getProjectsUseCase(10, state.searchFieldState.text.toString().takeIf { it.isNotBlank() })
        .cachedIn(viewModelScope)

    init {
        loadProjects()
    }

    // This method uses GetProjectsUseCase to load projects
    private fun loadProjects() {
        // Use the paginated version of GetProjectsUseCase
        getProjectsUseCase(10, state.searchFieldState.text.toString().takeIf { it.isNotBlank() })
    }

    private fun refreshProjects() {
        loadProjects()
    }

    fun updateSearchQuery(query: String) {
        val newState = TextFieldState()
        newState.edit { append(query) }
        state = state.copy(searchFieldState = newState)
        loadProjects()
    }

    private fun openProject(projectId: String) {
        // This method is now empty because navigation is handled in the UI layer
        // We keep it for compatibility with the existing code
    }

    fun handleActions(action: ProjectsAction) {
        when (action) {
            is ProjectsAction.LoadProjects -> loadProjects()
            is ProjectsAction.RefreshProjects -> refreshProjects()
            is ProjectsAction.OpenProjectDetails -> openProject(action.projectId)
            ProjectsAction.OpenCreateProject -> {} // Handled in UI layer
            is ProjectsAction.UpdateSearchQuery -> updateSearchQuery(action.query)
        }
    }
}
