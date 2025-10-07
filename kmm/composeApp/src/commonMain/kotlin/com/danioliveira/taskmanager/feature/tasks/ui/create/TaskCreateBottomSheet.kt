package com.danioliveira.taskmanager.feature.tasks.ui.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.DatePickerFieldToModal
import com.danioliveira.taskmanager.core.ui.components.TaskItCreateEditButtons
import com.danioliveira.taskmanager.core.ui.components.TaskItErrorMessage
import com.danioliveira.taskmanager.core.ui.components.TaskItFieldLabel
import com.danioliveira.taskmanager.core.ui.components.TaskItPriorityDropdown
import com.danioliveira.taskmanager.core.ui.components.TaskItStatusDropdown
import com.danioliveira.taskmanager.core.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.create_task
import kmmtaskmanager.composeapp.generated.resources.task_title_label
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateBottomSheet(
    onDismiss: () -> Unit,
    onTaskCreated: () -> Unit,
    projectId: String? = null,
    viewModel: TaskCreateViewModel = koinViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(projectId) {
        viewModel.initialize(projectId)
    }

    TaskCreateEffectHandler(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onTaskCreated = {
            onTaskCreated()
            onDismiss()
        }
    )

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onDismissRequest = onDismiss
    ) {
        TaskCreateContent(
            state = viewModel.state,
            onAction = viewModel::handleActions,
            onDismiss = onDismiss,
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
private fun TaskCreateContent(
    state: TaskCreateState,
    onAction: (TaskCreateAction) -> Unit,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.create_task),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }

        // Error message
        TaskItErrorMessage(
            errorMessage = state.titleError ?: state.descriptionError
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Title Field
            TaskItFieldLabel(stringResource(Res.string.task_title_label))
            TrackItInputField(
                state = state.titleFieldState,
                label = "Task Title",
                isError = state.titleError != null,
                errorMessage = state.titleError ?: "",
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description Field
            TaskItFieldLabel("Description")
            TrackItInputField(
                state = state.descriptionFieldState,
                label = "Description (optional)",
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3),
                isError = state.descriptionError != null,
                errorMessage = state.descriptionError ?: "",
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Dropdown
            TaskItFieldLabel("Priority")
            TaskItPriorityDropdown(
                currentPriority = state.selectedPriority,
                onPrioritySelected = { onAction(TaskCreateAction.UpdatePriority(it)) },
                expanded = priorityDropdownExpanded,
                onExpandedChange = { priorityDropdownExpanded = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Dropdown
            TaskItFieldLabel("Status")
            TaskItStatusDropdown(
                currentStatus = state.selectedStatus,
                onStatusSelected = { onAction(TaskCreateAction.UpdateStatus(it)) },
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date Picker
            TaskItFieldLabel("Due Date")
            DatePickerFieldToModal(
                selectedDate = state.selectedDueDate,
                onDateSelected = { onAction(TaskCreateAction.UpdateDueDate(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            TaskItCreateEditButtons(
                isCreating = true,
                isLoading = state.isSaving,
                isButtonEnabled = !state.isSaving,
                onCancel = onDismiss,
                onCreateOrUpdate = { onAction(TaskCreateAction.CreateTask) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun TaskCreateContentPreview() {
    TaskItTheme {
        TaskCreateContent(
            state = TaskCreateState(
                selectedPriority = Priority.HIGH,
                selectedStatus = TaskStatus.TODO,
                selectedDueDate = LocalDateTime.parse("2023-12-31T00:00:00")
            ),
            onAction = {},
            onDismiss = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview
@Composable
private fun TaskCreateContentWithErrorsPreview() {
    TaskItTheme {
        TaskCreateContent(
            state = TaskCreateState(
                titleError = "Title is required",
                descriptionError = "Description too long",
                isSaving = false
            ),
            onAction = {},
            onDismiss = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview
@Composable
private fun TaskCreateContentLoadingPreview() {
    TaskItTheme {
        TaskCreateContent(
            state = TaskCreateState(
                isSaving = true
            ),
            onAction = {},
            onDismiss = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}