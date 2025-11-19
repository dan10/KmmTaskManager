package com.danioliveira.taskmanager.feature.tasks.ui.create

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import kotlinx.datetime.LocalDateTime

/**
 * State for the Task Creation screen.
 */
@Stable
data class TaskCreateState(
    val titleFieldState: TextFieldState = TextFieldState(),
    val descriptionFieldState: TextFieldState = TextFieldState(),
    val selectedPriority: Priority = Priority.NONE,
    val selectedStatus: TaskStatus = TaskStatus.TODO,
    val selectedDueDate: LocalDateTime? = null,
    val projectId: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val titleError: String? = null,
    val descriptionError: String? = null
)

/**
 * Actions that can be performed on the Task Creation screen.
 */
sealed interface TaskCreateAction {
    data class UpdateTitle(val title: String) : TaskCreateAction
    data class UpdateDescription(val description: String) : TaskCreateAction
    data class UpdatePriority(val priority: Priority) : TaskCreateAction
    data class UpdateStatus(val status: TaskStatus) : TaskCreateAction
    data class UpdateDueDate(val dueDate: LocalDateTime?) : TaskCreateAction
    data class UpdateProjectId(val projectId: String?) : TaskCreateAction
    data object CreateTask : TaskCreateAction
    data object ClearErrors : TaskCreateAction
}

sealed class TaskCreateEffect {
    data class ShowSuccessSnackbar(
        val message: String
    ) : TaskCreateEffect()

    data class ShowErrorSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : TaskCreateEffect()

    data object TaskCreatedSuccessfully : TaskCreateEffect()
}