package com.danioliveira.taskmanager.feature.tasks.ui.create

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.core.domain.model.Priority
import com.danioliveira.taskmanager.core.domain.model.TaskStatus
import com.danioliveira.taskmanager.core.ui.components.DatePickerFieldToModal
import com.danioliveira.taskmanager.core.ui.components.TaskItCreateEditButtons
import com.danioliveira.taskmanager.core.ui.components.TaskItErrorMessage
import com.danioliveira.taskmanager.core.ui.components.TaskItInputField
import com.danioliveira.taskmanager.core.ui.components.TaskItPriorityDropdown
import com.danioliveira.taskmanager.testing.TestTags
import com.danioliveira.taskmanager.testing.enableTestTagsAsResourceId
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.create_task
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import perf.TraceLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreateBottomSheet(
    onDismiss: (shouldRefresh: Boolean) -> Unit,
    projectId: String? = null,
    viewModel: TaskCreateViewModel = koinViewModel()
) {
    TraceLifecycle("TaskCreateBottomSheet")
    
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    LaunchedEffect(projectId, sheetState.isVisible) {
        viewModel.initialize(projectId)
    }

    TaskCreateEffectHandler(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        onDismiss = onDismiss
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { onDismiss(false) },
        modifier = Modifier.enableTestTagsAsResourceId()
    ) {
        Box {
            TaskCreateContent(
                state = viewModel.state,
                onAction = viewModel::handleActions,
                snackbarHostState = snackbarHostState,
                onCancel = { onDismiss(false) }
            )
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun TaskCreateContent(
    state: TaskCreateState,
    onAction: (TaskCreateAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onCancel: () -> Unit
) {
    var priorityDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        TaskCreateHeader()
        TaskItErrorMessage(errorMessage = state.titleError ?: state.descriptionError)
        
        TaskCreateForm(
            state = state,
            priorityDropdownExpanded = priorityDropdownExpanded,
            onPriorityDropdownExpandedChange = { priorityDropdownExpanded = it },
            onAction = onAction,
            onCancel = onCancel
        )
    }
}

@Composable
private fun TaskCreateHeader() {
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
}

@Composable
private fun TaskCreateForm(
    state: TaskCreateState,
    priorityDropdownExpanded: Boolean,
    onPriorityDropdownExpandedChange: (Boolean) -> Unit,
    onAction: (TaskCreateAction) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        TaskCreateTitleField(state)
        Spacer(modifier = Modifier.height(16.dp))
        
        TaskCreateDescriptionField(state)
        Spacer(modifier = Modifier.height(16.dp))
        
        TaskItPriorityDropdown(
            currentPriority = state.selectedPriority,
            onPrioritySelected = { onAction(TaskCreateAction.UpdatePriority(it)) },
            expanded = priorityDropdownExpanded,
            onExpandedChange = onPriorityDropdownExpandedChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        DatePickerFieldToModal(
            selectedDate = state.selectedDueDate,
            onDateSelected = { onAction(TaskCreateAction.UpdateDueDate(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        TaskItCreateEditButtons(
            isCreating = true,
            isLoading = state.isSaving,
            isButtonEnabled = !state.isSaving,
            onCancel = onCancel,
            onCreateOrUpdate = { onAction(TaskCreateAction.CreateTask) }
        )
    }
}

@Composable
private fun TaskCreateTitleField(state: TaskCreateState) {
    TaskItInputField(
        state = state.titleFieldState,
        label = "Task Title",
        isError = state.titleError != null,
        errorMessage = state.titleError ?: "",
        enabled = !state.isSaving,
        lineLimits = TextFieldLineLimits.SingleLine,
        modifier = Modifier
            .fillMaxWidth(),
        textFieldModifier = Modifier.testTag(TestTags.TXT_TASK_TITLE)
    )
}

@Composable
private fun TaskCreateDescriptionField(state: TaskCreateState) {
    TaskItInputField(
        state = state.descriptionFieldState,
        label = "Description (optional)",
        lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 3),
        isError = state.descriptionError != null,
        errorMessage = state.descriptionError ?: "",
        enabled = !state.isSaving,
        modifier = Modifier
            .fillMaxWidth(),
        textFieldModifier = Modifier.testTag(TestTags.TXT_TASK_DESCRIPTION)
    )
}

@Preview
@Composable
private fun TaskCreateContentPreview() {
    TaskItTheme {
        TaskCreateContent(
            state = TaskCreateState(
                selectedPriority = Priority.NONE,
                selectedStatus = TaskStatus.TODO,
                selectedDueDate = LocalDateTime.parse("2023-12-31T00:00:00")
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            onCancel = {}
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
            snackbarHostState = remember { SnackbarHostState() },
            onCancel = {}
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
            snackbarHostState = remember { SnackbarHostState() },
            onCancel = {}
        )
    }
}