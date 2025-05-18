package com.danioliveira.taskmanager.ui.project.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.danioliveira.taskmanager.ui.components.TrackItInputField
import com.danioliveira.taskmanager.ui.theme.TaskItTheme
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.content_description_back
import kmmtaskmanager.composeapp.generated.resources.create_project
import kmmtaskmanager.composeapp.generated.resources.edit_project
import kmmtaskmanager.composeapp.generated.resources.project_cancel_button
import kmmtaskmanager.composeapp.generated.resources.project_create_button
import kmmtaskmanager.composeapp.generated.resources.project_description_label
import kmmtaskmanager.composeapp.generated.resources.project_name_error
import kmmtaskmanager.composeapp.generated.resources.project_name_label
import kmmtaskmanager.composeapp.generated.resources.project_update_button
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

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
        topBar = {
            ProjectTopAppBar(
                isCreating = state.isCreating,
                onBack = onBack
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
            ProjectFormFields(
                state = state
            )

            Spacer(modifier = Modifier.weight(1f))

            // Buttons
            ProjectActionButtons(
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
private fun ProjectTopAppBar(
    isCreating: Boolean,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isCreating) stringResource(Res.string.create_project) else stringResource(Res.string.edit_project)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.content_description_back)
                )
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
private fun ProjectFormFields(
    state: CreateEditProjectState
) {
    // Project Name field
    TrackItInputField(
        state = state.projectName,
        label = stringResource(Res.string.project_name_label),
        isError = state.projectNameHasError,
        errorMessage = stringResource(Res.string.project_name_error),
        enabled = !state.isLoading,
        lineLimits = TextFieldLineLimits.SingleLine
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Description field
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

@Composable
private fun ProjectActionButtons(
    isCreating: Boolean,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    onCancel: () -> Unit,
    onCreateOrUpdate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colors.primary)
        ) {
            Text(stringResource(Res.string.project_cancel_button))
        }

        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

        Button(
            onClick = onCreateOrUpdate,
            modifier = Modifier.weight(1f),
            enabled = isButtonEnabled && !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (isCreating) stringResource(Res.string.project_create_button) else stringResource(Res.string.project_update_button),
                    color = Color.White
                )
            }
        }
    }
}

// --- Previews ---
@Preview
@Composable
fun CreateProjectScreenPreview() {
    TaskItTheme {
        CreateEditProjectScreen(
            state = CreateEditProjectState(
                isCreating = true,
                projectName = TextFieldState("Mobile App"),
                description = TextFieldState("Develop a new task management app."),
                isButtonEnabled = true
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
                isButtonEnabled = false
            ),
            onBack = {},
            actions = {}
        )
    }
}
