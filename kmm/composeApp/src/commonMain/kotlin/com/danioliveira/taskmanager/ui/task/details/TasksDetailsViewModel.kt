package com.danioliveira.taskmanager.ui.task.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.data.mapper.toTask
import com.danioliveira.taskmanager.domain.usecase.tasks.DeleteTaskUseCase
import com.danioliveira.taskmanager.domain.usecase.tasks.GetTaskDetailsUseCase
import kotlinx.coroutines.launch

class TasksDetailsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getTaskDetailsUseCase: GetTaskDetailsUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    var state by mutableStateOf(TasksDetailsState())
        private set

    var onBack: () -> Unit = {}
    var onEditTask: (String) -> Unit = {}

    init {
        loadTaskDetails()
    }

    private fun loadTaskDetails() {
        val taskId = savedStateHandle.get<String>("taskId")
        if (taskId != null) {
            state = state.copy(isLoading = true, errorMessage = null)
            viewModelScope.launch {
                getTaskDetailsUseCase(taskId)
                    .onSuccess { taskResponse ->
                        state = state.copy(
                            isLoading = false,
                            task = taskResponse.toTask(),
                            errorMessage = null
                        )
                    }
                    .onFailure { error ->
                        state = state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load task details"
                        )
                    }
            }
        } else {
            state = state.copy(
                isLoading = false,
                errorMessage = "Task ID not found"
            )
        }
    }

    fun handleActions(action: TasksDetailsAction) {
        when (action) {
            is TasksDetailsAction.LoadTaskDetails -> loadTaskDetails()
            is TasksDetailsAction.NavigateBack -> navigateBack()
            is TasksDetailsAction.EditTask -> editTask()
            is TasksDetailsAction.DeleteTask -> deleteTask()
        }
    }

    private fun navigateBack() {
        onBack()
    }

    private fun editTask() {
        state.task?.let { task ->
            onEditTask(task.id.toString())
        }
    }

    private fun deleteTask() {
        state.task?.let { task ->
            state = state.copy(isDeleting = true, errorMessage = null)
            viewModelScope.launch {
                deleteTaskUseCase(task.id.toString())
                    .onSuccess {
                        state = state.copy(isDeleting = false)
                        onBack() // Navigate back after successful deletion
                    }
                    .onFailure { error ->
                        state = state.copy(
                            isDeleting = false,
                            errorMessage = error.message ?: "Failed to delete task"
                        )
                    }
            }
        }
    }
}
