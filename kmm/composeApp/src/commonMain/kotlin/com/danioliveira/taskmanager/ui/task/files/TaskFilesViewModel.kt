package com.danioliveira.taskmanager.ui.task.files

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.data.mapper.toFiles
import com.danioliveira.taskmanager.domain.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskFilesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository
) : ViewModel() {

    var state by mutableStateOf(TaskFilesState())
        private set

    var onBack: () -> Unit = {}

    init {
        loadFiles()
    }

    private fun loadFiles() {
        val taskId = savedStateHandle.get<String>("taskId")
        if (taskId != null) {
            state = state.copy(isLoading = true, errorMessage = null, taskId = taskId)
            viewModelScope.launch {
                taskRepository.getTaskFiles(taskId)
                    .onSuccess { fileResponses ->
                        state = state.copy(
                            isLoading = false,
                            files = fileResponses.toFiles(),
                            errorMessage = null
                        )
                    }
                    .onFailure { error ->
                        state = state.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load task files"
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

    fun handleActions(action: TaskFilesAction) {
        when (action) {
            is TaskFilesAction.LoadFiles -> loadFiles()
            is TaskFilesAction.NavigateBack -> navigateBack()
        }
    }

    private fun navigateBack() {
        onBack()
    }
}