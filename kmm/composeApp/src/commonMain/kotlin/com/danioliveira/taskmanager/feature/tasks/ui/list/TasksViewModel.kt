package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.DeleteTaskUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTaskProgressUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.GetTasksUseCase
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.UpdateTaskStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class TasksViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val getTaskProgressUseCase: GetTaskProgressUseCase,
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    var state by mutableStateOf(TasksState())
        private set

    // Use a SharedFlow to trigger refresh of the paging data
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    private val _events = MutableSharedFlow<TaskUiEvent>()
    val events: SharedFlow<TaskUiEvent> = _events.asSharedFlow()

    val taskFlow = refreshTrigger
        .flatMapLatest {
            val searchQuery = state.searchFieldState.text.toString().takeIf { it.isNotBlank() }
            getTasksUseCase(10, searchQuery)
        }
        .cachedIn(viewModelScope)

    init {
        // Trigger initial load
        refreshTrigger.tryEmit(Unit)
        loadTaskProgress()

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

    private fun updateTaskStatus(taskId: Uuid, status: TaskStatus) {
        viewModelScope.launch {
            updateTaskStatusUseCase(taskId.toString(), status)
                .onSuccess {
                    refreshTasks()
                }
                .onFailure {
                    refreshTasks()
                    _events.emit(
                        TaskUiEvent.ShowSnackbar(
                            message = "Failed to update task",
                            actionLabel = "Retry"
                        ) {
                            updateTaskStatus(taskId, status)
                        }
                    )
                }
        }
    }

    private fun deleteTask(taskId: Uuid) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId.toString())
                .onSuccess {
                    refreshTasks()
                }
                .onFailure {
                    refreshTasks()
                    _events.emit(
                        TaskUiEvent.ShowSnackbar(
                            message = "Failed to delete task",
                            actionLabel = "Retry"
                        ) {
                            deleteTask(taskId)
                        }
                    )
                }
        }
    }

    private fun clearSearch() {
        state = state.copy(searchFieldState = TextFieldState())
    }

    fun updateSearchQuery(query: String) {
        state = state.copy(searchFieldState = TextFieldState(query))
    }
    
    private fun toggleStatusFilter(status: TaskStatus) {
        state = if (state.selectedStatusFilters.contains(status)) {
            state.copy(selectedStatusFilters = state.selectedStatusFilters - status)
        } else {
            state.copy(selectedStatusFilters = state.selectedStatusFilters + status)
        }
    }
    
    private fun togglePriorityFilter(priority: Priority) {
        state = if (state.selectedPriorityFilters.contains(priority)) {
            state.copy(selectedPriorityFilters = state.selectedPriorityFilters - priority)
        } else {
            state.copy(selectedPriorityFilters = state.selectedPriorityFilters + priority)
        }
    }
    
    private fun changeSortOption(sortOption: TaskSortOption) {
        state = state.copy(sortOption = sortOption)
    }
    
    private fun clearAllFilters() {
        state = state.copy(
            selectedStatusFilters = emptySet(),
            selectedPriorityFilters = emptySet()
        )
    }
    
    private fun toggleFilterExpanded() {
        state = state.copy(isFilterExpanded = !state.isFilterExpanded)
    }

    fun handleActions(action: TasksAction) {
        when (action) {
            is TasksAction.LoadTasks -> loadTasks()
            is TasksAction.RefreshTasks -> refreshTasks()
            is TasksAction.UpdateTaskStatus -> updateTaskStatus(action.taskId, action.status)
            is TasksAction.DeleteTask -> deleteTask(action.taskId)
            is TasksAction.ClearSearch -> clearSearch()
            is TasksAction.ToggleStatusFilter -> toggleStatusFilter(action.status)
            is TasksAction.TogglePriorityFilter -> togglePriorityFilter(action.priority)
            is TasksAction.ChangeSortOption -> changeSortOption(action.sortOption)
            is TasksAction.ClearAllFilters -> clearAllFilters()
            is TasksAction.ToggleFilterExpanded -> toggleFilterExpanded()
            is TasksAction.SetSearchQuery -> updateSearchQuery(action.query)
            else -> Unit
        }
    }

    sealed class TaskUiEvent {
        data class ShowSnackbar(
            val message: String,
            val actionLabel: String? = null,
            val onAction: (() -> Unit)? = null
        ) : TaskUiEvent()
    }
}
