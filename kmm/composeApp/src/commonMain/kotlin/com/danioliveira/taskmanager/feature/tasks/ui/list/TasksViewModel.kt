package com.danioliveira.taskmanager.feature.tasks.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
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

    private val _effects = MutableSharedFlow<TasksEffect>()
    val effects: SharedFlow<TasksEffect> = _effects.asSharedFlow()

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
                    val message = if (status == TaskStatus.DONE) "Task completed!" else "Task marked as incomplete"
                    _effects.emit(TasksEffect.ShowSuccessSnackbar(message))
                }
                .onFailure {
                    refreshTasks()
                    _effects.emit(
                        TasksEffect.ShowErrorSnackbar(
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
                    _effects.emit(TasksEffect.ShowSuccessSnackbar("Task deleted successfully"))
                }
                .onFailure {
                    refreshTasks()
                    _effects.emit(
                        TasksEffect.ShowErrorSnackbar(
                            message = "Failed to delete task",
                            actionLabel = "Retry"
                        ) {
                            deleteTask(taskId)
                        }
                    )
                }
        }
    }

    private fun confirmTaskCompletion(taskId: Uuid, status: TaskStatus) {
        viewModelScope.launch {
            val actionText = if (status == TaskStatus.DONE) "complete" else "mark as incomplete"
            _effects.emit(
                TasksEffect.ShowConfirmationSnackbar(
                    message = "Are you sure you want to $actionText this task?",
                    actionLabel = "Confirm"
                ) {
                    updateTaskStatus(taskId, status)
                }
            )
        }
    }

    private fun confirmTaskDeletion(taskId: Uuid) {
        viewModelScope.launch {
            _effects.emit(
                TasksEffect.ShowConfirmationSnackbar(
                    message = "Are you sure you want to delete this task?",
                    actionLabel = "Delete"
                ) {
                    deleteTask(taskId)
                }
            )
        }
    }

    fun handleActions(action: TasksAction) {
        when (action) {
            is TasksAction.LoadTasks -> loadTasks()
            is TasksAction.RefreshTasks -> refreshTasks()
            is TasksAction.UpdateTaskStatus -> updateTaskStatus(action.taskId, action.status)
            is TasksAction.DeleteTask -> deleteTask(action.taskId)
            is TasksAction.ConfirmTaskCompletion -> confirmTaskCompletion(action.taskId, action.status)
            is TasksAction.ConfirmTaskDeletion -> confirmTaskDeletion(action.taskId)
            is TasksAction.OpenTaskDetails -> navigateToTaskDetail(action.taskId)
            is TasksAction.OpenCreateTask -> showCreateTaskBottomSheet()
        }
    }

    private fun navigateToTaskDetail(taskId: Uuid) {
        viewModelScope.launch {
            _effects.emit(TasksEffect.NavigateToTaskDetail(taskId))
        }
    }

    private fun showCreateTaskBottomSheet() {
        viewModelScope.launch {
            _effects.emit(TasksEffect.ShowCreateTaskBottomSheet)
        }
    }
}
