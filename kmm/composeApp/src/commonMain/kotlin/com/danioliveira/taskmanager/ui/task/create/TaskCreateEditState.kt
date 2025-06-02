package com.danioliveira.taskmanager.ui.task.create

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import kotlinx.datetime.LocalDateTime

/**
 * State class for the TaskCreatEditScreen.
 */
data class TaskCreateEditState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val taskId: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val title: TextFieldState = TextFieldState(),
    val description: TextFieldState = TextFieldState(),
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDateTime? = null,
    val showDatePicker: Boolean = false,
    val status: TaskStatus = TaskStatus.TODO,
    val isCreating: Boolean = true
) {
    val titleHasError
        get() = title.text.isEmpty()

    private val titleIsNotEmpty
        get() = title.text.isNotEmpty()

    val isFormValid
        get() = titleIsNotEmpty

    val isButtonEnabled
        get() = isFormValid && !isLoading
}

/**
 * Actions that can be performed on the TaskCreateEditState.
 */
sealed interface TaskCreateEditAction {
    data object CreateTask : TaskCreateEditAction
    data object UpdateTask : TaskCreateEditAction
    data object DeleteTask : TaskCreateEditAction
    data class SetPriority(val priority: Priority) : TaskCreateEditAction
    data class SetStatus(val status: TaskStatus) : TaskCreateEditAction
    data object ShowDatePicker : TaskCreateEditAction
    data object HideDatePicker : TaskCreateEditAction
    data class SetDate(val date: LocalDateTime) : TaskCreateEditAction
}
