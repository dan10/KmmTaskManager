package com.danioliveira.taskmanager.ui.projects

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val getProjectsUseCase: GetProjectsUseCase
) : ViewModel() {

    var state by mutableStateOf(ProjectsState())
        private set

    // Use a SharedFlow to trigger refresh of the paging data
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val projectFlow = refreshTrigger
        .flatMapLatest {
            val searchQuery = state.searchFieldState.text.toString().takeIf { it.isNotBlank() }
            getProjectsUseCase(10, searchQuery)
        }
        .cachedIn(viewModelScope)

    init {
        // Trigger initial load
        refreshTrigger.tryEmit(Unit)
        loadProjects()
        
        // Watch for search text changes
        viewModelScope.launch {
            snapshotFlow { state.searchFieldState.text }
                .distinctUntilChanged()
                .collect { searchText ->
                    // Trigger refresh when search text changes
                    refreshTrigger.tryEmit(Unit)
                }
        }
    }

    // This method uses GetProjectsUseCase to load projects
    private fun loadProjects() {
        // Trigger refresh of the paging data
        refreshTrigger.tryEmit(Unit)
    }

    private fun refreshProjects() {
        loadProjects()
    }

    fun checkAndRefresh() {
        refreshProjects()
    }

    fun updateSearchQuery(query: String) {
        val newState = TextFieldState()
        newState.edit { append(query) }
        state = state.copy(searchFieldState = newState)
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
