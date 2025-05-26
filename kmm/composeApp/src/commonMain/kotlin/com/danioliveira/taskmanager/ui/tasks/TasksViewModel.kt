package com.danioliveira.taskmanager.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.danioliveira.taskmanager.domain.usecase.tasks.GetTaskProgressUseCase
import com.danioliveira.taskmanager.domain.usecase.tasks.GetTasksUseCase
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskProgressUseCase: GetTaskProgressUseCase,
) : ViewModel() {

    var state by mutableStateOf(TasksState())
        private set

    var searchQuery by mutableStateOf("")
        private set

    val taskFlow = getTasksUseCase(10, searchQuery.takeIf { it.isNotBlank() })
        .cachedIn(viewModelScope)

    init {
        loadTasks()
        loadTaskProgress()
    }

    // This method uses GetTasksUseCase to load tasks
    private fun loadTasks() {
        // Use the paginated version of GetTasksUseCase
        getTasksUseCase(10, searchQuery.takeIf { it.isNotBlank() })
    }

    // This method uses GetTaskProgressUseCase to load progress information for the screen header
    private fun loadTaskProgress() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            getTaskProgressUseCase()
                .onSuccess { progress ->
                    state = state.copy(
                        isLoading = false,
                        completedTasks = progress.completedTasks,
                        totalTasks = progress.totalTasks
                    )
                }
                .onFailure { error ->

                }
        }
    }

    private fun refreshTasks() {
        loadTasks()
        loadTaskProgress()
    }


    fun updateSearchQuery(query: String) {
        searchQuery = query
        loadTasks()
    }


    private fun openTask(taskId: Uuid) {
        // This method is now empty because navigation is handled in the UI layer
        // We keep it for compatibility with the existing code
    }

    fun handleActions(action: TasksAction) {
        when(action) {
            is TasksAction.LoadTasks -> loadTasks()
            is TasksAction.RefreshTasks -> refreshTasks()
            is TasksAction.OpenTaskDetails -> openTask(action.taskId)
            TasksAction.OpenCreateTask -> TODO()
            is TasksAction.UpdateSearchQuery -> updateSearchQuery(action.query)
        }
    }
}
