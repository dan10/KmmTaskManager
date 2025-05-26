package com.danioliveira.taskmanager.ui.project.create

import androidx.compose.foundation.text.input.TextFieldState

/**
 * State for the CreateEditProject screen.
 */
data class CreateEditProjectState(
    val isCreating: Boolean = true,
    val projectId: String? = null,
    val projectName: TextFieldState = TextFieldState(""),
    val description: TextFieldState = TextFieldState(""),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val projectNameHasError: Boolean = false,
    val isButtonEnabled: Boolean = false // Logic for this will be in ViewModel
)

/**
 * Actions for the CreateEditProject screen.
 */
sealed interface CreateEditProjectAction {
    data object CreateProject : CreateEditProjectAction
    data object UpdateProject : CreateEditProjectAction
}