package com.danioliveira.taskmanager.ui.project.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.danioliveira.taskmanager.data.mapper.toProject
import com.danioliveira.taskmanager.domain.Task
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectDetailsUseCase
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectTasksUseCase
import com.danioliveira.taskmanager.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ProjectDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getProjectDetailsUseCase: GetProjectDetailsUseCase,
    private val getProjectTasksUseCase: GetProjectTasksUseCase
) : ViewModel() {

    var state by mutableStateOf(ProjectDetailsState())
        private set

    var onBack: () -> Unit = {}
    var onCreateTask: (String) -> Unit = {}

    val projectId = savedStateHandle.toRoute<Screen.ProjectDetails>().projectId

    // Use a SharedFlow to trigger refresh of the paging data
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    // Create a Flow of PagingData<Task> for the project tasks
    val taskFlow: Flow<PagingData<Task>> = refreshTrigger
        .flatMapLatest {
            getProjectTasksUseCase(projectId)
        }
        .cachedIn(viewModelScope)

    init {
        loadProjectDetails()
        // Trigger initial load of tasks
        refreshTrigger.tryEmit(Unit)
    }

    private fun loadProjectDetails() {
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            getProjectDetailsUseCase(projectId)
                .onSuccess { projectResponse ->
                    state = state.copy(
                        isLoading = false,
                        project = projectResponse.toProject(),
                        errorMessage = null
                    )
                }
                .onFailure { error ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load project details"
                    )
                }
        }
    }

    private fun refreshTasks() {
        // Trigger refresh of the paging data
        refreshTrigger.tryEmit(Unit)
    }

    fun checkAndRefresh() {
        loadProjectDetails()
        refreshTasks()
    }

    private fun updateTaskStatus(taskId: String, status: String) {
        // This would be implemented to update a task's status
        // For now, we'll just log it
        println("Updating task $taskId status to $status")
    }

    private fun createTask() {
        state.project?.id?.let { projectId ->
            onCreateTask(projectId)
        }
    }

    fun handleActions(action: ProjectDetailsAction) {
        when (action) {
            is ProjectDetailsAction.RefreshTasks -> refreshTasks()
            is ProjectDetailsAction.UpdateTaskStatus -> updateTaskStatus(action.taskId, action.status)
            is ProjectDetailsAction.CreateTask -> createTask()
        }
    }
}
