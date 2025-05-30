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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest

class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskProgressUseCase: GetTaskProgressUseCase,
) : ViewModel() {

    var state by mutableStateOf(TasksState())
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Use a SharedFlow to trigger refresh of the paging data
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    val taskFlow = refreshTrigger
        .flatMapLatest {
            getTasksUseCase(10, searchQuery.takeIf { it.isNotBlank() })
        }
        .cachedIn(viewModelScope)


    // This method uses GetTasksUseCase to load tasks
    private fun loadTasks() {
        // Trigger refresh of the paging data
        refreshTrigger.tryEmit(Unit)
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

    // Public method to refresh tasks - can be called from outside
    fun refresh() {
        refreshTasks()
    }

    // Method to check if refresh is needed and perform it
    fun checkAndRefresh() {
        // Always refresh when this method is called
        // This will be called from the UI when returning from task operations
        refresh()
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
