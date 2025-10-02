package com.danioliveira.taskmanager.ui.project.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.components.TaskItCreateEditButtons
import com.danioliveira.taskmanager.ui.components.TaskItErrorMessage
import com.danioliveira.taskmanager.ui.components.TaskItTopAppBar
import com.danioliveira.taskmanager.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.create_project
import kmmtaskmanager.composeapp.generated.resources.edit_project
import kmmtaskmanager.composeapp.generated.resources.project_description_label
import kmmtaskmanager.composeapp.generated.resources.project_name_error
import kmmtaskmanager.composeapp.generated.resources.project_name_label
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * BottomSheet version of project creation/editing screen
 */
@Composable
fun CreateEditProjectBottomSheet(
    projectId: String?,
    onDismiss: () -> Unit = {},
    viewModel: CreateEditProjectViewModel = koinViewModel(key = "bottomsheet_${projectId}")
) {
    // Initialize the ViewModel with the projectId
    LaunchedEffect(projectId) {
        viewModel.initialize(projectId)
    }
    
    viewModel.onProjectCreated = onDismiss
    viewModel.onProjectUpdated = onDismiss

    val state by viewModel.uiState.collectAsState()
    CreateEditProjectBottomSheetContent(state, onDismiss, viewModel::handleActions)
}

@Composable
private fun CreateEditProjectBottomSheetContent(
    state: CreateEditProjectState,
    onDismiss: () -> Unit,
    actions: (CreateEditProjectAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header with title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    if (state.isCreating) Res.string.create_project else Res.string.edit_project
                ),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Error message
            TaskItErrorMessage(errorMessage = state.errorMessage)

            // Form fields
            ProjectFormFields(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            TaskItCreateEditButtons(
                isCreating = state.isCreating,
                isLoading = state.isLoading,
                isButtonEnabled = state.isButtonEnabled,
                onCancel = onDismiss,
                onCreateOrUpdate = {
                    if (state.isCreating) {
                        actions(CreateEditProjectAction.CreateProject)
                    } else {
                        actions(CreateEditProjectAction.UpdateProject)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CreateEditProjectScreen(
    onBack: () -> Unit = {},
    viewModel: CreateEditProjectViewModel = koinViewModel()
) {
    viewModel.onProjectCreated = onBack
    viewModel.onProjectUpdated = onBack

    val state by viewModel.uiState.collectAsState()

    CreateEditProjectScreen(
        state = state,
        onBack = onBack,
        actions = viewModel::handleActions
    )
}

@Composable
private fun CreateEditProjectScreen(
    state: CreateEditProjectState,
    onBack: () -> Unit,
    actions: (CreateEditProjectAction) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.ime,
        topBar = {
            TaskItTopAppBar(
                title = stringResource(
                    if (state.isCreating) Res.string.create_project
                    else Res.string.edit_project
                ),
                onNavigateBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TaskItErrorMessage(errorMessage = state.errorMessage)

            ProjectFormFields(
                state = state
            )

            Spacer(modifier = Modifier.weight(1f))

            TaskItCreateEditButtons(
                isCreating = state.isCreating,
                isLoading = state.isLoading,
                isButtonEnabled = state.isButtonEnabled,
                onCancel = onBack,
                onCreateOrUpdate = {
                    if (state.isCreating) {
                        actions(CreateEditProjectAction.CreateProject)
                    } else {
                        actions(CreateEditProjectAction.UpdateProject)
                    }
                }
            )
        }
    }
}

@Composable
private fun ProjectFormFields(
    state: CreateEditProjectState
) {
    TrackItInputField(
        state = state.projectName,
        label = stringResource(Res.string.project_name_label),
        isError = state.projectNameHasError,
        errorMessage = stringResource(Res.string.project_name_error),
        enabled = !state.isLoading,
        lineLimits = TextFieldLineLimits.SingleLine
    )

    Spacer(modifier = Modifier.height(16.dp))

    TrackItInputField(
        state = state.description,
        label = stringResource(Res.string.project_description_label),
        isError = false,
        errorMessage = "",
        enabled = !state.isLoading,
        lineLimits = TextFieldLineLimits.Default,
        modifier = Modifier.height(120.dp)
    )
}

@Preview
@Composable
fun CreateProjectScreenPreview() {
    TaskItTheme {
        CreateEditProjectScreen(
            state = CreateEditProjectState(
                isCreating = true,
                projectName = TextFieldState("Mobile App"),
                description = TextFieldState("Develop a new task management app."),
            ),
            onBack = {},
            actions = {}
        )
    }
}

@Preview
@Composable
fun CreateProjectScreenLoadingPreview() {
    TaskItTheme {
        CreateEditProjectScreen(
            state = CreateEditProjectState(
                isCreating = true,
                projectName = TextFieldState("Website Redesign"),
                description = TextFieldState("Complete redesign of the company website."),
                isLoading = true,
            ),
            onBack = {},
            actions = {}
        )
    }
}
