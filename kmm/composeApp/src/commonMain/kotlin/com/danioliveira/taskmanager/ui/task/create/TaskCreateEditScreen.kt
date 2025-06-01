package com.danioliveira.taskmanager.ui.task.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.domain.Priority
import com.danioliveira.taskmanager.ui.components.DatePickerFieldToModal
import com.danioliveira.taskmanager.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.content_description_delete
import kmmtaskmanager.composeapp.generated.resources.create_task
import kmmtaskmanager.composeapp.generated.resources.edit_task
import kmmtaskmanager.composeapp.generated.resources.task_cancel_button
import kmmtaskmanager.composeapp.generated.resources.task_create_button
import kmmtaskmanager.composeapp.generated.resources.task_description_label
import kmmtaskmanager.composeapp.generated.resources.task_due_date_label
import kmmtaskmanager.composeapp.generated.resources.task_priority_label
import kmmtaskmanager.composeapp.generated.resources.task_title_error
import kmmtaskmanager.composeapp.generated.resources.task_title_label
import kmmtaskmanager.composeapp.generated.resources.task_update_button
import kmmtaskmanager.composeapp.generated.resources.project_name_label
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import com.danioliveira.taskmanager.utils.PriorityFormatter

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TaskCreateEditScreen(
    onBack: () -> Unit = {},
    viewModel: TaskCreateEditViewModel = koinViewModel()
) {
    // Set navigation callbacks
        viewModel.onTaskCreated = onBack
        viewModel.onTaskUpdated = onBack
        viewModel.onTaskDeleted = onBack

    // Collect UI state
    val state by viewModel.uiState.collectAsState()
    TaskCreateEditScreen(state, onBack, viewModel::handleActions)
}

@Composable
private fun TaskCreateEditScreen(
    state: TaskCreateEditState,
    onBack: () -> Unit,
    actions: (TaskCreateEditAction) -> Unit
) {
    var priorityDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TaskTopAppBar(
                isCreating = state.isCreating,
                onBack = onBack,
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
            ErrorMessage(errorMessage = state.errorMessage)

            // Form fields
            TaskFormFields(
                state = state,
                priorityDropdownExpanded = priorityDropdownExpanded,
                onPriorityDropdownExpandedChange = { priorityDropdownExpanded = it },
                onPrioritySelected = { actions(TaskCreateEditAction.SetPriority(it)) },
                onDateSelected = { actions(TaskCreateEditAction.SetDate(it)) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            TaskActionButtons(
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
private fun TaskTopAppBar(
    isCreating: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = stringResource(
                    if (isCreating) Res.string.create_task else Res.string.edit_task
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
            }
        },
        actions = {
            if (!isCreating) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.content_description_delete)
                    )
                }
            }
        }
    )
}

@Composable
private fun ErrorMessage(errorMessage: String?) {
    errorMessage?.let { error ->
        Text(
            text = error,
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun TaskFormFields(
    state: TaskCreateEditState,
    priorityDropdownExpanded: Boolean,
    onPriorityDropdownExpandedChange: (Boolean) -> Unit,
    onPrioritySelected: (Priority) -> Unit,
    onDateSelected: (LocalDateTime) -> Unit
) {
    // Project field (if project is associated)
    if (state.projectName != null) {
        Text(
            text = stringResource(Res.string.project_name_label),
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        
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
    Text(
        text = stringResource(Res.string.task_priority_label),
        style = MaterialTheme.typography.caption,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )

    PriorityDropdown(
        currentPriority = state.priority,
        expanded = priorityDropdownExpanded,
        onExpandedChange = onPriorityDropdownExpandedChange,
        onPrioritySelected = onPrioritySelected
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Due date field
    DatePickerFieldToModal(
        selectedDate = state.dueDate,
        onDateSelected = onDateSelected,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PriorityDropdown(
    currentPriority: Priority,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPrioritySelected: (Priority) -> Unit
) {
    OutlinedButton(
        onClick = { onExpandedChange(true) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(PriorityFormatter.formatPriority(currentPriority))
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            Priority.entries.forEach { priorityOption ->
                DropdownMenuItem(
                    onClick = {
                        onPrioritySelected(priorityOption)
                        onExpandedChange(false)
                    }
                ) {
                    Text(PriorityFormatter.formatPriority(priorityOption))
                }
            }
        }
    }
}

@Composable
private fun TaskActionButtons(
    isCreating: Boolean,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onCancel: () -> Unit,
    onCreateOrUpdate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !isLoading
        ) {
            Text(stringResource(Res.string.task_cancel_button))
        }

        Button(
            onClick = onCreateOrUpdate,
            modifier = Modifier.weight(1f),
            enabled = isButtonEnabled
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text(
                    text = stringResource(
                        if (isCreating) Res.string.task_create_button
                        else Res.string.task_update_button
                    ),
                    color = Color.White
                )
            }
        }
    }
}

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
