package com.danioliveira.taskmanager.ui.task.create

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.usecase.tasks.CreateEditTaskUseCase
import com.danioliveira.taskmanager.domain.usecase.projects.GetProjectDetailsUseCase
import com.danioliveira.taskmanager.data.mapper.toProject
import com.danioliveira.taskmanager.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class TaskCreateEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val createEditTaskUseCase: CreateEditTaskUseCase,
    private val getProjectDetailsUseCase: GetProjectDetailsUseCase
) : ViewModel() {

    // Navigation callbacks to be set from outside
    var onTaskCreated: () -> Unit = {}
    var onTaskUpdated: () -> Unit = {}
    var onTaskDeleted: () -> Unit = {}

    private val _uiState = MutableStateFlow(TaskCreateEditState())
    val uiState: StateFlow<TaskCreateEditState> = _uiState.asStateFlow()

    init {
        val route = savedStateHandle.toRoute<Screen.CreateEditTask>()
        val taskId = route.taskId
        val projectId = route.projectId
        initialize(taskId, projectId)
    }

    /**
     * Initializes the ViewModel with an existing task if editing and project if specified.
     *
     * @param taskId The ID of the task to edit, or null if creating a new task
     * @param projectId The ID of the project to associate the task with, or null if no project
     */
    fun initialize(taskId: String?, projectId: String?) {
        if (taskId == null) {
            // Creating a new task
            _uiState.update { it.copy(isCreating = true, projectId = projectId) }
            if (projectId != null) {
                loadProjectDetails(projectId)
            }
        } else {
            // Editing an existing task
            _uiState.update { 
                it.copy(isCreating = false, taskId = taskId, projectId = projectId, isLoading = true) 
            }
            loadTask(taskId)
            if (projectId != null) {
                loadProjectDetails(projectId)
            }
        }
    }

    /**
     * Loads project details for the given project ID.
     *
     * @param projectId The ID of the project to load
     */
    private fun loadProjectDetails(projectId: String) {
        viewModelScope.launch {
            val result = getProjectDetailsUseCase(projectId)
            result.fold(
                onSuccess = { projectResponse ->
                    val project = projectResponse.toProject()
                    _uiState.update { state ->
                        state.copy(projectName = project.name)
                    }
                },
                onFailure = { error ->
                    // Don't show error for project loading failure, just leave project name empty
                    println("Failed to load project details: ${error.message}")
                }
            )
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
                    dueDate = dueDate,
                    projectId = projectId,
                    assigneeId = null // Don't auto-assign - user must be a project member
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
}
