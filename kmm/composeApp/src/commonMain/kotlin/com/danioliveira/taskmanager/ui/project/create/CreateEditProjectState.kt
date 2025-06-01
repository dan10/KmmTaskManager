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
) {

    val projectNameHasError
        get() = projectName.text.trim().isEmpty()

    private val projectNameIsNotEmpty
        get() = projectName.text.trim().isNotEmpty()

    val isFormValid
        get() = projectNameIsNotEmpty

    val isButtonEnabled
        get() = isFormValid && !isLoading
}

/**
 * Actions for the CreateEditProject screen.
 */
sealed interface CreateEditProjectAction {
    data object CreateProject : CreateEditProjectAction
    data object UpdateProject : CreateEditProjectAction
}