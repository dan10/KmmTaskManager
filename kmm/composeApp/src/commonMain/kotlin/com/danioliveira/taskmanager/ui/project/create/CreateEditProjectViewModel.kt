package com.danioliveira.taskmanager.ui.project.create

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.danioliveira.taskmanager.domain.usecase.projects.CreateEditProjectUseCase
import com.danioliveira.taskmanager.navigation.Screen
import kmmtaskmanager.composeapp.generated.resources.Res
import kmmtaskmanager.composeapp.generated.resources.project_create_error
import kmmtaskmanager.composeapp.generated.resources.project_load_error
import kmmtaskmanager.composeapp.generated.resources.project_update_error
import kmmtaskmanager.composeapp.generated.resources.project_update_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString

/**
 * ViewModel for the CreateEditProject screen.
 */
@OptIn(ExperimentalResourceApi::class)
class CreateEditProjectViewModel(
    savedStateHandle: SavedStateHandle,
    private val createEditProjectUseCase: CreateEditProjectUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateEditProjectState())
    val uiState: StateFlow<CreateEditProjectState> = _uiState.asStateFlow()

    var onProjectCreated: () -> Unit = {}
    var onProjectUpdated: () -> Unit = {}

    init {
        val projectId = savedStateHandle.toRoute<Screen.CreateEditProject>().projectId
        initialize(projectId)
    }

    /**
     * Initializes the ViewModel with an existing project if editing.
     *
     * @param projectId The ID of the project to edit, or null if creating a new project
     */
    private fun initialize(projectId: String?) {
        if (projectId == null) {
            // Creating a new project
            _uiState.update {
                it.copy(isCreating = true)
            }
        } else {
            // Editing an existing project
            loadProject(projectId)
        }
    }

    /**
     * Loads an existing project for editing.
     *
     * @param projectId The ID of the project to load
     */
    private fun loadProject(projectId: String) {
        _uiState.update { it.copy(isCreating = false, projectId = projectId, isLoading = true) }
        viewModelScope.launch {
            createEditProjectUseCase.getProject(projectId).fold(
                onSuccess = { project ->
                    _uiState.update {
                        it.copy(
                            projectName = TextFieldState(project.name),
                            description = TextFieldState(project.description ?: ""),
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    viewModelScope.launch {
                        val errorMessage =
                            getString(Res.string.project_load_error, error.message ?: "")
                        _uiState.update {
                            it.copy(
                                errorMessage = errorMessage,
                                isLoading = false
                            )
                        }
                    }
                }
            )
        }
    }

    /**
     * Handles actions from the UI.
     *
     * @param action The action to handle
     */
    fun handleActions(action: CreateEditProjectAction) {
        when (action) {
            is CreateEditProjectAction.CreateProject -> createProject()
            is CreateEditProjectAction.UpdateProject -> updateProject()
        }
    }

    /**
     * Validates the form inputs.
     *
     * @return True if the form is valid, false otherwise
     */
    private fun validateForm(): Boolean {
        val state = _uiState.value
        return state.isFormValid
    }

    /**
     * Creates a new project.
     */
    private fun createProject() {
        if (!validateForm()) return

        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val name = state.projectName.text.trim().toString()
            val description = state.description.text.trim().takeIf { it.isNotEmpty() }?.toString()

            createEditProjectUseCase.createProject(name, description).fold(
                onSuccess = { project ->
                    _uiState.update { it.copy(isLoading = false) }
                    onProjectCreated()
                },
                onFailure = { error ->
                    viewModelScope.launch {
                        val errorMessage =
                            getString(Res.string.project_create_error, error.message ?: "")
                        _uiState.update {
                            it.copy(
                                errorMessage = errorMessage,
                                isLoading = false
                            )
                        }
                    }
                }
            )
        }
    }

    /**
     * Updates an existing project.
     */
    private fun updateProject() {
        if (!validateForm()) return

        val state = _uiState.value
        val projectId = state.projectId ?: return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val name = state.projectName.text.trim().toString()
            val description = state.description.text.trim().takeIf { it.isNotEmpty() }?.toString()

            createEditProjectUseCase.updateProject(projectId, name, description).fold(
                onSuccess = { success ->
                    if (success) {
                        _uiState.update { it.copy(isLoading = false) }
                        onProjectUpdated()
                    } else {
                        viewModelScope.launch {
                            val errorMessage = getString(Res.string.project_update_failed)
                            _uiState.update {
                                it.copy(
                                    errorMessage = errorMessage,
                                    isLoading = false
                                )
                            }
                        }
                    }
                },
                onFailure = { error ->
                    viewModelScope.launch {
                        val errorMessage =
                            getString(Res.string.project_update_error, error.message ?: "")
                        _uiState.update {
                            it.copy(
                                errorMessage = errorMessage,
                                isLoading = false
                            )
                        }
                    }
                }
            )
        }
    }
}
