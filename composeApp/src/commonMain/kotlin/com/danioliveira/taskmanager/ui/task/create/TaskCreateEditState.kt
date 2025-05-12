package com.danioliveira.taskmanager.ui.task.create

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus

/**
 * State class for the TaskCreatEditScreen.
 */
data class TaskCreateEditState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val taskId: String? = null,
    val title: TextFieldState = TextFieldState(),
    val description: TextFieldState = TextFieldState(),
    val priority: Priority = Priority.MEDIUM,
    val dueDate: TextFieldState = TextFieldState(),
    val status: TaskStatus = TaskStatus.TODO,
    val isCreating: Boolean = true
) {
    val titleHasError
        get() = title.text.isEmpty()

    val dueDateHasError
        get() = dueDate.text.isNotEmpty() && !isValidDateFormat(dueDate.text.toString())

    private val titleIsNotEmpty
        get() = title.text.isNotEmpty()

    val isFormValid
        get() = titleIsNotEmpty && !dueDateHasError

    val isButtonEnabled
        get() = isFormValid && !isLoading

    /**
     * Validates if the date is in the format DD/MM/YYYY.
     */
    private fun isValidDateFormat(date: String): Boolean {
        val regex = Regex("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$")
        return regex.matches(date)
    }
}

/**
 * Actions that can be performed on the TaskCreateEditState.
 */
sealed interface TaskCreateEditAction {
    data object CreateTask : TaskCreateEditAction
    data object UpdateTask : TaskCreateEditAction
    data object DeleteTask : TaskCreateEditAction
    data class SetPriority(val priority: Priority) : TaskCreateEditAction
}
