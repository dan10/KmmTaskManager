package com.danioliveira.taskmanager.ui.task.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.domain.TaskStatus
import com.danioliveira.taskmanager.ui.components.DatePickerFieldToModal
import com.danioliveira.taskmanager.ui.components.TaskItCreateEditButtons
import com.danioliveira.taskmanager.ui.components.TaskItCreateEditTopAppBar
import com.danioliveira.taskmanager.ui.components.TaskItErrorMessage
import com.danioliveira.taskmanager.ui.components.TaskItFieldLabel
import com.danioliveira.taskmanager.ui.components.TaskItPriorityDropdown
import com.danioliveira.taskmanager.ui.components.TaskItStatusDropdown
import com.danioliveira.taskmanager.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.create_task
import kmmtaskmanager.composeapp.generated.resources.edit_task
import kmmtaskmanager.composeapp.generated.resources.project_name_label
import kmmtaskmanager.composeapp.generated.resources.task_description_label
import kmmtaskmanager.composeapp.generated.resources.task_priority_label
import kmmtaskmanager.composeapp.generated.resources.task_status_label
import kmmtaskmanager.composeapp.generated.resources.task_title_error
import kmmtaskmanager.composeapp.generated.resources.task_title_label
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TaskCreateEditScreen(
    onBack: () -> Unit = {},
    viewModel: TaskCreateEditViewModel = koinViewModel()
) {
    viewModel.onTaskCreated = onBack
    viewModel.onTaskUpdated = onBack
    viewModel.onTaskDeleted = onBack


    val state by viewModel.uiState.collectAsState()
    TaskCreateEditScreen(state, onBack, viewModel::handleActions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCreateEditScreen(
    state: TaskCreateEditState,
    onBack: () -> Unit,
    actions: (TaskCreateEditAction) -> Unit
) {
    var priorityDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TaskItCreateEditTopAppBar(
                title = stringResource(
                    if (state.isCreating) Res.string.create_task else Res.string.edit_task
                ),
                onNavigateBack = onBack,
                showDeleteAction = !state.isCreating,
                onDelete = { actions(TaskCreateEditAction.DeleteTask) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Error message
            TaskItErrorMessage(errorMessage = state.errorMessage)

            // Form fields
            TaskFormFields(
                state = state,
                priorityDropdownExpanded = priorityDropdownExpanded,
                onPriorityDropdownExpandedChange = { priorityDropdownExpanded = it },
                onPrioritySelected = { actions(TaskCreateEditAction.SetPriority(it)) },
                statusDropdownExpanded = statusDropdownExpanded,
                onStatusDropdownExpandedChange = { statusDropdownExpanded = it },
                onStatusSelected = { actions(TaskCreateEditAction.SetStatus(it)) },
                onDateSelected = { actions(TaskCreateEditAction.SetDate(it)) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            TaskItCreateEditButtons(
                isCreating = state.isCreating,
                isLoading = state.isLoading,
                isButtonEnabled = state.isButtonEnabled,
                onCancel = onBack,
                onCreateOrUpdate = {
                    if (state.isCreating) {
                        actions(TaskCreateEditAction.CreateTask)
                    } else {
                        actions(TaskCreateEditAction.UpdateTask)
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskFormFields(
    state: TaskCreateEditState,
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

    // Status dropdown - only show when editing a task
    if (!state.isCreating) {
        TaskItFieldLabel(stringResource(Res.string.task_status_label))

        TaskItStatusDropdown(
            currentStatus = state.status,
            expanded = statusDropdownExpanded,
            onExpandedChange = onStatusDropdownExpandedChange,
            onStatusSelected = onStatusSelected
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Due date field
    DatePickerFieldToModal(
        selectedDate = state.dueDate,
        onDateSelected = onDateSelected,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun TaskScreenPreview() {
    TaskItTheme {
        TaskCreateEditScreen(
            state = TaskCreateEditState(
                isCreating = false,
                title = TextFieldState("Title"),
                description = TextFieldState("Description"),
                priority = Priority.LOW,
                dueDate = Clock.System.now().plus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()),
                isLoading = false
            ),
            onBack = {},
            actions = {}
        )
    }
}

@Preview
@Composable
fun CreateTaskScreenPreview() {
    TaskItTheme {
        TaskCreateEditScreen(
            onBack = {},
            actions = {},
            state = TaskCreateEditState(isCreating = true)
        )
    }
}
