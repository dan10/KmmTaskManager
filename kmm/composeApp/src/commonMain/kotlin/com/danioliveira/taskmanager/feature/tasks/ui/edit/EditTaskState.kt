package com.danioliveira.taskmanager.feature.tasks.ui.edit

import androidx.compose.foundation.text.input.TextFieldState
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import kotlinx.datetime.LocalDateTime

/**
 * State class for the EditTaskScreen.
 */
data class EditTaskState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val taskId: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val title: TextFieldState = TextFieldState(),
    val description: TextFieldState = TextFieldState(),
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDateTime? = null,
    val showDatePicker: Boolean = false,
    val status: TaskStatus = TaskStatus.TODO
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
 * Actions that can be performed on the EditTaskState.
 */
sealed interface EditTaskAction {
    data object UpdateTask : EditTaskAction
    data object DeleteTask : EditTaskAction
    data class SetPriority(val priority: Priority) : EditTaskAction
    data class SetStatus(val status: TaskStatus) : EditTaskAction
    data object ShowDatePicker : EditTaskAction
    data object HideDatePicker : EditTaskAction
    data class SetDate(val date: LocalDateTime) : EditTaskAction
}

/**
 * Effects for EditTask screen (side effects like navigation, snackbars)
 */
sealed interface EditTaskEffect {
    data class ShowSuccessSnackbar(val message: String) : EditTaskEffect
    data class ShowErrorSnackbar(val message: String) : EditTaskEffect
    data object TaskUpdatedSuccessfully : EditTaskEffect
    data object TaskDeletedSuccessfully : EditTaskEffect
}

