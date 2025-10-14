package com.danioliveira.taskmanager.feature.tasks.ui.edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.DatePickerFieldToModal
import com.danioliveira.taskmanager.core.ui.components.TaskItFieldLabel
import com.danioliveira.taskmanager.core.ui.components.TaskItPriorityDropdown
import com.danioliveira.taskmanager.core.ui.components.TaskItStatusDropdown
import com.danioliveira.taskmanager.core.ui.components.TrackItInputField
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.project_name_label
import kmmtaskmanager.composeapp.generated.resources.task_description_label
import kmmtaskmanager.composeapp.generated.resources.task_priority_label
import kmmtaskmanager.composeapp.generated.resources.task_status_label
import kmmtaskmanager.composeapp.generated.resources.task_title_error
import kmmtaskmanager.composeapp.generated.resources.task_title_label
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditTaskFormFields(
    state: EditTaskState,
    priorityDropdownExpanded: Boolean,
    onPriorityDropdownExpandedChange: (Boolean) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    statusDropdownExpanded: Boolean,
    onStatusDropdownExpandedChange: (Boolean) -> Unit,
    onStatusSelected: (TaskStatus) -> Unit,
    onDateSelected: (LocalDateTime) -> Unit
) {
    // Project field (if project is associated)
    if (state.projectName != null) {
        TaskItFieldLabel(stringResource(Res.string.project_name_label))

        OutlinedTextField(
            value = state.projectName,
            onValueChange = { /* Read-only field */ },
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Title field
    TrackItInputField(
        state = state.title,
        label = stringResource(Res.string.task_title_label),
        isError = state.titleHasError,
        errorMessage = stringResource(Res.string.task_title_error),
        enabled = !state.isLoading,
        lineLimits = TextFieldLineLimits.SingleLine
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Description field
    TrackItInputField(
        state = state.description,
        label = stringResource(Res.string.task_description_label),
        isError = false,
        errorMessage = "",
        enabled = !state.isLoading,
        lineLimits = TextFieldLineLimits.Default
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Priority dropdown
    TaskItFieldLabel(stringResource(Res.string.task_priority_label))

    TaskItPriorityDropdown(
        currentPriority = state.priority,
        expanded = priorityDropdownExpanded,
        onExpandedChange = onPriorityDropdownExpandedChange,
        onPrioritySelected = onPrioritySelected
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Status dropdown
    TaskItFieldLabel(stringResource(Res.string.task_status_label))

    TaskItStatusDropdown(
        currentStatus = state.status,
        expanded = statusDropdownExpanded,
        onExpandedChange = onStatusDropdownExpandedChange,
        onStatusSelected = onStatusSelected
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Due date field
    DatePickerFieldToModal(
        selectedDate = state.dueDate,
        onDateSelected = onDateSelected,
        modifier = Modifier.fillMaxWidth()
    )
}

