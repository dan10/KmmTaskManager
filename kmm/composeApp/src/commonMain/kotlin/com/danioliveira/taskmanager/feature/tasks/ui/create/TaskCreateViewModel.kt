package com.danioliveira.taskmanager.feature.tasks.ui.create

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.feature.tasks.domain.usecase.tasks.CreateEditTaskUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

class TaskCreateViewModel(
    private val createEditTaskUseCase: CreateEditTaskUseCase
) : ViewModel() {

    var state by mutableStateOf(TaskCreateState())
        private set

    private val _effects = MutableSharedFlow<TaskCreateEffect>()
    val effects: SharedFlow<TaskCreateEffect> = _effects.asSharedFlow()

    fun initialize(projectId: String?) {
        state = TaskCreateState(projectId = projectId)
    }

    fun handleActions(action: TaskCreateAction) {
        when (action) {
            is TaskCreateAction.UpdateTitle -> updateTitle(action.title)
            is TaskCreateAction.UpdateDescription -> updateDescription(action.description)
            is TaskCreateAction.UpdatePriority -> updatePriority(action.priority)
            is TaskCreateAction.UpdateStatus -> updateStatus(action.status)
            is TaskCreateAction.UpdateDueDate -> updateDueDate(action.dueDate)
            is TaskCreateAction.UpdateProjectId -> updateProjectId(action.projectId)
            is TaskCreateAction.CreateTask -> createTask()
            is TaskCreateAction.ClearErrors -> clearErrors()
        }
    }

    private fun updateTitle(title: String) {
        state.titleFieldState.setTextAndPlaceCursorAtEnd(title)
        if (state.titleError != null) {
            state = state.copy(titleError = null)
        }
    }

    private fun updateDescription(description: String) {
        state.descriptionFieldState.setTextAndPlaceCursorAtEnd(description)
        if (state.descriptionError != null) {
            state = state.copy(descriptionError = null)
        }
    }

    private fun updatePriority(priority: Priority) {
        state = state.copy(selectedPriority = priority)
    }

    private fun updateStatus(status: TaskStatus) {
        state = state.copy(selectedStatus = status)
    }

    private fun updateDueDate(dueDate: LocalDateTime?) {
        state = state.copy(selectedDueDate = dueDate)
    }

    private fun updateProjectId(projectId: String?) {
        state = state.copy(projectId = projectId)
    }

    private fun clearErrors() {
        state = state.copy(
            titleError = null,
            descriptionError = null
        )
    }

    private fun createTask() {
        if (!validateInput()) return

        viewModelScope.launch {
            state = state.copy(isSaving = true)

            val title = state.titleFieldState.text.toString().trim()
            val description = state.descriptionFieldState.text.toString().trim()

            createEditTaskUseCase.createTask(
                title = title,
                description = description.ifEmpty { null },
                priority = state.selectedPriority,
                dueDate = state.selectedDueDate,
                projectId = state.projectId
            ).onSuccess {
                state = state.copy(isSaving = false)
                _effects.emit(TaskCreateEffect.ShowSuccessSnackbar("Task created successfully"))
                _effects.emit(TaskCreateEffect.TaskCreatedSuccessfully)
            }.onFailure { error ->
                state = state.copy(isSaving = false)
                _effects.emit(
                    TaskCreateEffect.ShowErrorSnackbar(
                        message = "Failed to create task: ${error.message}",
                        actionLabel = "Retry"
                    ) {
                        createTask()
                    }
                )
            }
        }
    }

    private fun validateInput(): Boolean {
        val title = state.titleFieldState.text.toString().trim()
        var isValid = true

        if (title.isEmpty()) {
            state = state.copy(titleError = "Title is required")
            isValid = false
        }

        if (title.length > 100) {
            state = state.copy(titleError = "Title must be less than 100 characters")
            isValid = false
        }

        val description = state.descriptionFieldState.text.toString().trim()
        if (description.length > 500) {
            state = state.copy(descriptionError = "Description must be less than 500 characters")
            isValid = false
        }

        return isValid
    }

}