package com.danioliveira.taskmanager.feature.tasks.ui.edit

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.CreateEditTaskUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.uuid.Uuid

class EditTaskViewModel(
    private val taskId: String,
    private val createEditTaskUseCase: CreateEditTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTaskState(taskId = taskId))
    val uiState: StateFlow<EditTaskState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<EditTaskEffect>()
    val effects: SharedFlow<EditTaskEffect> = _effects.asSharedFlow()

    init {
        loadTask()
    }

    /**
     * Loads the task data for editing.
     */
    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = createEditTaskUseCase.getTask(Uuid.parse(taskId))

            result.fold(
                onSuccess = { task ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            title = TextFieldState(task.title),
                            description = TextFieldState(task.description),
                            priority = task.priority,
                            dueDate = task.dueDate,
                            status = task.status,
                            projectId = task.projectId,
                            projectName = task.projectName,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load task"
                        )
                    }
                    _effects.emit(
                        EditTaskEffect.ShowErrorSnackbar(
                            message = error.message ?: "Failed to load task"
                        )
                    )
                }
            )
        }
    }

    /**
     * Updates the existing task.
     */
    private fun updateTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = with(uiState.value) {
                createEditTaskUseCase.updateTask(
                    taskId = taskId!!,
                    title = title.text.toString(),
                    description = description.text.toString().takeIf { it.isNotEmpty() },
                    priority = priority,
                    dueDate = dueDate,
                    status = status
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(EditTaskEffect.ShowSuccessSnackbar("Task updated successfully"))
                    _effects.emit(EditTaskEffect.TaskUpdatedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to update task"
                        )
                    }
                    _effects.emit(
                        EditTaskEffect.ShowErrorSnackbar(
                            message = error.message ?: "Failed to update task"
                        )
                    )
                }
            )
        }
    }

    /**
     * Deletes the existing task.
     */
    private fun deleteTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = createEditTaskUseCase.deleteTask(taskId)

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(EditTaskEffect.ShowSuccessSnackbar("Task deleted successfully"))
                    _effects.emit(EditTaskEffect.TaskDeletedSuccessfully)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to delete task"
                        )
                    }
                    _effects.emit(
                        EditTaskEffect.ShowErrorSnackbar(
                            message = error.message ?: "Failed to delete task"
                        )
                    )
                }
            )
        }
    }

    /**
     * Sets the priority of the task.
     */
    private fun setPriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    /**
     * Sets the status of the task.
     */
    private fun setStatus(status: TaskStatus) {
        _uiState.update { it.copy(status = status) }
    }

    /**
     * Shows the date picker.
     */
    private fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    /**
     * Hides the date picker.
     */
    private fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    /**
     * Sets the selected date.
     */
    private fun setDate(date: LocalDateTime) {
        _uiState.update {
            it.copy(
                dueDate = date
            )
        }
    }

    /**
     * Handles actions from the UI.
     */
    fun handleActions(action: EditTaskAction) {
        when (action) {
            is EditTaskAction.UpdateTask -> updateTask()
            is EditTaskAction.DeleteTask -> deleteTask()
            is EditTaskAction.SetPriority -> setPriority(action.priority)
            is EditTaskAction.SetStatus -> setStatus(action.status)
            is EditTaskAction.ShowDatePicker -> showDatePicker()
            is EditTaskAction.HideDatePicker -> hideDatePicker()
            is EditTaskAction.SetDate -> setDate(action.date)
        }
    }
}

