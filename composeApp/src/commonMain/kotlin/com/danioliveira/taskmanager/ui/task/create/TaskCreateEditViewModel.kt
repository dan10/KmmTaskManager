package com.danioliveira.taskmanager.ui.task.create

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.usecase.tasks.CreateEditTaskUseCase
import com.danioliveira.taskmanager.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class TaskCreateEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val createEditTaskUseCase: CreateEditTaskUseCase
) : ViewModel() {

    // Navigation callbacks to be set from outside
    var onTaskCreated: () -> Unit = {}
    var onTaskUpdated: () -> Unit = {}
    var onTaskDeleted: () -> Unit = {}

    private val _uiState = MutableStateFlow(TaskCreateEditState())
    val uiState: StateFlow<TaskCreateEditState> = _uiState.asStateFlow()

    init {
        val taskId = savedStateHandle.toRoute<Screen.CreateEditTask>().taskId
        initialize(taskId)
    }

    /**
     * Initializes the ViewModel with an existing task if editing.
     *
     * @param taskId The ID of the task to edit, or null if creating a new task
     */
    fun initialize(taskId: String?) {
        if (taskId == null) {
            // Creating a new task
            _uiState.update { it.copy(isCreating = true) }
        } else {
            // Editing an existing task
            _uiState.update { it.copy(isCreating = false, taskId = taskId, isLoading = true) }
            loadTask(taskId)
        }
    }

    /**
     * Loads an existing task for editing.
     *
     * @param taskId The ID of the task to load
     */
    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            val result = createEditTaskUseCase.getTask(taskId)

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
                }
            )
        }
    }

    /**
     * Creates a new task.
     */
    private fun createTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = with(uiState.value) {
                createEditTaskUseCase.createTask(
                    title = title.text.toString(),
                    description = description.text.toString().takeIf { it.isNotEmpty() },
                    priority = priority,
                    dueDate = dueDate
                )
            }

            result.fold(
                onSuccess = {
                    onTaskCreated()
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to create task"
                        )
                    }
                }
            )
        }
    }

    /**
     * Updates an existing task.
     */
    private fun updateTask() {
        viewModelScope.launch {
            val taskId = uiState.value.taskId ?: return@launch

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = with(uiState.value) {
                createEditTaskUseCase.updateTask(
                    taskId = taskId,
                    title = title.text.toString(),
                    description = description.text.toString().takeIf { it.isNotEmpty() },
                    priority = priority,
                    dueDate = dueDate,
                    status = status
                )
            }

            result.fold(
                onSuccess = {
                    onTaskUpdated()
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to update task"
                        )
                    }
                }
            )
        }
    }

    /**
     * Deletes an existing task.
     */
    private fun deleteTask() {
        viewModelScope.launch {
            val taskId = uiState.value.taskId ?: return@launch

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = createEditTaskUseCase.deleteTask(taskId)

            result.fold(
                onSuccess = {
                    onTaskDeleted()
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to delete task"
                        )
                    }
                }
            )
        }
    }

    /**
     * Sets the priority of the task.
     *
     * @param priority The new priority
     */
    private fun setPriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    /**
     * Handles actions from the UI.
     *
     * @param action The action to handle
     */
    fun handleActions(action: TaskCreateEditAction) {
        when (action) {
            is TaskCreateEditAction.CreateTask -> createTask()
            is TaskCreateEditAction.UpdateTask -> updateTask()
            is TaskCreateEditAction.DeleteTask -> deleteTask()
            is TaskCreateEditAction.SetPriority -> setPriority(action.priority)
            is TaskCreateEditAction.ShowDatePicker -> showDatePicker()
            is TaskCreateEditAction.HideDatePicker -> hideDatePicker()
            is TaskCreateEditAction.SetDate -> setDate(action.date)
        }
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
     *
     * @param date The selected date
     */
    private fun setDate(date: LocalDateTime) {
        _uiState.update {
            it.copy(
                dueDate = date
            )
        }
    }

    /**
     * Formats a LocalDateTime to a string in the format DD/MM/YYYY.
     *
     * @param date The date to format
     * @return The formatted date string
     */
    private fun formatDate(date: LocalDateTime): String {
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${
            date.monthNumber.toString().padStart(2, '0')
        }/${date.year}"
    }
}
