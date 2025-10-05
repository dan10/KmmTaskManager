package com.danioliveira.taskmanager.feature.projects.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.danioliveira.taskmanager.feature.projects.domain.usecase.GetProjectsUseCase
import com.danioliveira.taskmanager.ui.projects.ProjectsAction
import com.danioliveira.taskmanager.ui.projects.ProjectsState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest

class ProjectsViewModel(
    private val getProjectsUseCase: GetProjectsUseCase
) : ViewModel() {

    var state by mutableStateOf(ProjectsState())
        private set

    // Use a SharedFlow to trigger refresh of the paging data
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val projectFlow = refreshTrigger
        .flatMapLatest {
            getProjectsUseCase(10, null)
        }
        .cachedIn(viewModelScope)

    init {
        // Trigger initial load
        refreshTrigger.tryEmit(Unit)
        loadProjects()
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

    fun handleActions(action: ProjectsAction) {
        when (action) {
            is ProjectsAction.LoadProjects -> loadProjects()
            is ProjectsAction.RefreshProjects -> refreshProjects()
            is ProjectsAction.OpenProjectDetails -> Unit
            ProjectsAction.OpenCreateProject -> Unit
        }
    }
}
